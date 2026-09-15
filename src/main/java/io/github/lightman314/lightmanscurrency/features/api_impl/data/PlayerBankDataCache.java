package io.github.lightman314.lightmanscurrency.features.api_impl.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.api.data.FancyData;
import io.github.lightman314.lightmanscurrency.api.data.FancyDataType;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.proxy.LCProxy;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.*;

public class PlayerBankDataCache extends FancyData {

    private static final Codec<BankEntry> ENTRY_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            BankAccount.CODEC.fieldOf("account").forGetter(e -> e.account),
            BankReference.CODEC.fieldOf("selected").forGetter(e -> e.selected)
    ).apply(builder,BankEntry::new));
    public static final Codec<PlayerBankDataCache> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.unboundedMap(UUIDUtil.STRING_CODEC,ENTRY_CODEC).fieldOf("data").forGetter(d -> d.playerAccounts),
            Codec.LONG.fieldOf("lastInterest").forGetter(d -> d.interestTimeStamp)
    ).apply(builder,PlayerBankDataCache::new));

    public static final FancyDataType<PlayerBankDataCache> TYPE = new FancyDataType<>("lightmanscurrency_bank_data",PlayerBankDataCache::new,CODEC);

    private final Map<UUID,BankEntry> playerAccounts = new HashMap<>();
    private long interestTimeStamp = 0;
    public long getLastInterestTime() {
        if(this.interestTimeStamp == 0) //If no interest time has been logged, start the timer now
            this.interestTimeStamp = TimeHelper.getCurrentTime();
        return this.interestTimeStamp;
    }
    public void flagInterestAsComplete() { this.interestTimeStamp = TimeHelper.getCurrentTime(); this.setChanged(); }

    private Set<UUID> changedAccounts = new HashSet<>();

    private PlayerBankDataCache() {  }
    private PlayerBankDataCache(Map<UUID,BankEntry> playerAccounts,long interestTimeStamp) {
        this.playerAccounts.putAll(playerAccounts);
        this.playerAccounts.forEach((player,entry) -> {
            this.attachAccount(player,entry.account);
            entry.selected.setSidedContext(this);
        });
        this.interestTimeStamp = interestTimeStamp;
    }

    @Override
    public FancyDataType<?> getType() { return TYPE; }

    private BankAccount generateBankAccount(UUID player) {
        BankAccount account = new BankAccount();
        this.attachAccount(player,account);
        return account;
    }

    private void attachAccount(UUID player,BankAccount account) {
        account.setListener(() -> this.setAccountChanged(player));
        account.setNotificationConsumer(BankAccount.generateNotificationAcceptor(player));
        if(this.isServer())//Only update the name on the server
            account.updateOwnersName(PlayerReference.of(player,account.getOwnersName()).getName(this));
        account.setSidedContext(this);
    }

    private void setAccount(UUID player,BankAccount account) {
        if(player == null)
            return;
        this.attachAccount(player,account);
        BankEntry entry = this.playerAccounts.get(player);
        if(entry == null)
            entry = new BankEntry(account,PlayerBankReference.of(player).setSidedContext(this));
        entry.account = account;
        this.playerAccounts.put(player,entry);
    }

    public List<BankReference> getPlayerBankAccounts() {
        List<BankReference> results = new ArrayList<>();
        for(UUID player : this.playerAccounts.keySet())
            results.add(PlayerBankReference.of(player));
        return results;
    }

    public boolean hasAccount(UUID player) { return this.playerAccounts.containsKey(player); }

    public BankAccount getAccount(Player player) { return this.getAccount(player.getUUID()); }
    public BankAccount getAccount(UUID player) {
        if(player == null)
            return null;
        if(this.playerAccounts.containsKey(player))
            return this.playerAccounts.get(player).account;
        BankAccount newAccount = this.generateBankAccount(player);
        this.playerAccounts.put(player,new BankEntry(newAccount,PlayerBankReference.of(player).setSidedContext(this)));
        //Send account creation packet to all
        this.sendInitialAccount(player);
        return newAccount;
    }

    public boolean deleteAccount(UUID player) {
        MinecraftServer server = this.getServer();
        if(server == null)
            return false;
        if(!this.playerAccounts.containsKey(player))
            return false;
        //If the player whose bank account got deleted is online, delete and replace their bank account/data
        ServerPlayer onlinePlayer = server.getPlayerList().getPlayer(player);
        this.playerAccounts.remove(player);
        //Player is online, so create a new bank account after deleting the old one
        if(onlinePlayer != null) {
            //Since it's going to create a new bank account,
            //this will also set the account as changed and automatically send the sync packet
            this.getAccount(player);
            //Send sync packet for their new selected bank account
            this.sendInitialAccount(player);
        }
        else {
            //Player is NOT online, so don't re-create it after deletion
            this.setChanged();
            //Send deleted packet to all connected clients
            this.sendPacketToAll(FancyPacketMap.map().setUUID("delete",player));
        }
        return true;
    }

    public void setAccountChanged(UUID player) {
        if(this.isClient())
            return;
        this.setChanged();
        this.changedAccounts.add(player);
    }

    private void sendInitialAccount(UUID player) { this.sendInitialAccount(player,null); }
    private void sendInitialAccount(UUID player,@Nullable ServerPlayer target) {
        BankAccount account = this.getAccount(player);
        this.sendPacket(target,FancyPacketMap.map()
                .setMap("create",FancyPacketMap.map()
                        .setUUID("id",player)
                        .set("account",LCFancyPacketTypes.BANK_ACCOUNT,account)));
    }

    public BankReference getSelectedAccount(Player player) {
        UUID playerID = player.getUUID();
        if(this.playerAccounts.containsKey(playerID)) {
            BankReference account = this.playerAccounts.get(playerID).selected;
            //Confirm the selected account is valid/allowed
            if(!account.allowedAccess(player)) {
                account = PlayerBankReference.of(player).setSidedContext(this);
                this.setSelectedAccount(player,account);
            }
            return account;
        }
        //Generate default bank account for the player
        BankReference account = PlayerBankReference.of(player).setSidedContext(this);
        this.setSelectedAccount(player,account);
        return account;
    }

    public void setSelectedAccount(Player player,BankReference account) {
        if(account == null)
            return;
        if(this.isClient()) {
            if(!LCProxy.get().isSelf(player))
                return;
            //TODO send c2s packet requesting the selected account change
        }
        if(!account.allowedAccess(player))
            return;
        UUID playerID = player.getUUID();
        BankEntry entry = this.playerAccounts.get(playerID);
        if(entry == null) {
            entry = new BankEntry(this.generateBankAccount(playerID),null);
            this.sendInitialAccount(playerID);
        }
        entry.selected = account.setSidedContext(this);
        this.setChanged();
        if(player instanceof ServerPlayer sp)
            this.syncSelectedAccount(sp);
    }

    private void syncSelectedAccount(ServerPlayer player) {
        this.sendPacketToTarget(player,FancyPacketMap.map()
                .setMap("selected",FancyPacketMap.map()
                        .setUUID("id",player.getUUID())
                        .set("account",LCFancyPacketTypes.BANK_REFERENCE,this.getSelectedAccount(player))));
    }

    @Override
    public void onPlayerJoin(ServerPlayer player) {
        //Send initial account packets for each account
        for(UUID id : new HashSet<>(this.playerAccounts.keySet()))
            this.sendInitialAccount(id,player);
        //Then confirm the player-specific account is loaded (so that no duplicate packets are sent if it wasn't)
        this.getAccount(player);
        //Then sync the selected account
        this.syncSelectedAccount(player);
    }

    @Override
    public void syncTick() {
        Set<UUID> updated = this.changedAccounts;
        this.changedAccounts = new HashSet<>();
        for(UUID id : updated) {
            BankEntry entry = this.playerAccounts.get(id);
            if(entry == null)
                continue;
            //Send the update packet
            FancyPacketMap packet = entry.account.getAndCleanPacket().mutable().setUUID("id",id);
            this.sendPacketToAll(FancyPacketMap.map().setMap("update",packet));
        }
    }

    @Override
    protected void handleSyncPacket(FancyPacketMap data) {
        if(data.contains("create")) {
            FancyPacketMap entry = data.getMap("create");
            UUID id = entry.getUUID("id");
            BankAccount account = entry.get("account",LCFancyPacketTypes.BANK_ACCOUNT);
            this.setAccount(id,account);
        }
        if(data.contains("delete")) {
            UUID id = data.getUUID("delete");
            if(id != null)
                this.playerAccounts.remove(id);
        }
        if(data.contains("selected")) {
            FancyPacketMap entry = data.getMap("selected");
            UUID id = entry.getUUID("id");
            BankReference account = entry.get("account",LCFancyPacketTypes.BANK_REFERENCE);
            if(id != null) {
                BankEntry e = this.playerAccounts.get(id);
                if(e == null) {
                    this.setAccount(id,new BankAccount());
                    e = this.playerAccounts.get(id);
                }
                e.selected = account.setSidedContext(this);
            }
        }
        if(data.contains("update")) {
            FancyPacketMap update = data.getMap("update");
            BankAccount account = this.getAccount(update.getUUID("id"));
            if(account != null)
                account.handlePacket(update);
        }
    }

    private static class BankEntry {
        BankAccount account;
        BankReference selected;
        BankEntry(BankAccount account,BankReference selected) { this.account = account; this.selected = selected; }
        void setContext(ISidedContext context) { this.account.setSidedContext(context); this.selected.setSidedContext(context); }
    }

}
