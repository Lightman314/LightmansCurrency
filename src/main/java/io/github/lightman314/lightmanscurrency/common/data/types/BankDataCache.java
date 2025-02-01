package io.github.lightman314.lightmanscurrency.common.data.types;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomData;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomDataType;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.network.message.bank.CPacketSelectBankAccount;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BankDataCache extends CustomData {

    public static final CustomDataType<BankDataCache> TYPE = new CustomDataType<>("lightmanscurrency_bank_data",BankDataCache::new);

    private final Map<UUID,BankDataEntry> playerBankData = new HashMap<>();
    private int interestTick = 0;

    private BankDataCache() {}

    @Override
    public CustomDataType<?> getType() { return TYPE; }

    @Override
    public void save(CompoundTag tag) {
        ListTag bankData = new ListTag();
        this.playerBankData.forEach((player,data) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Player", player);
            entry.put("BankAccount", data.account.save());
            entry.put("LastSelected", data.selected.save());
            bankData.add(entry);
        });
        tag.put("PlayerBankData", bankData);

        tag.putInt("InterestTick", this.interestTick);
    }

    @Override
    protected void load(CompoundTag tag) {
        ListTag bankData = tag.getList("PlayerBankData", Tag.TAG_COMPOUND);
        for(int i = 0; i < bankData.size(); ++i)
        {
            CompoundTag entry = bankData.getCompound(i);
            UUID player = entry.getUUID("Player");
            BankAccount bankAccount = this.loadBankAccount(player, entry.getCompound("BankAccount"));
            BankReference lastSelected = BankReference.load(entry.getCompound("LastSelected"));
            playerBankData.put(player, new BankDataEntry(bankAccount,lastSelected));
        }
        if(tag.contains("InterestTick"))
            this.interestTick = tag.getInt("InterestTick");
    }

    private BankAccount loadBankAccount(UUID player, CompoundTag compound) {
        BankAccount bankAccount = new BankAccount(() -> this.markAccountDirty(player), compound);
        try {
            bankAccount.setNotificationConsumer(BankAccount.generateNotificationAcceptor(player));
            bankAccount.updateOwnersName(PlayerReference.of(player, bankAccount.getOwnersName()).getName(false));
        } catch(Throwable ignored) {  }
        return bankAccount;
    }

    private BankAccount generateBankAccount(UUID player) {
        BankAccount bankAccount = new BankAccount(() -> this.markAccountDirty(player));
        try {
            bankAccount.setNotificationConsumer(BankAccount.generateNotificationAcceptor(player));
            bankAccount.updateOwnersName(PlayerReference.of(player, bankAccount.getOwnersName()).getName(this.isClient()));
        } catch(Throwable ignored) { }
        return bankAccount;
    }

    public List<BankReference> getPlayerBankAccounts() {
        List<BankReference> results = new ArrayList<>();
        for(UUID player : this.playerBankData.keySet())
            results.add(PlayerBankReference.of(player).flagAsClient(this));
        return results;
    }

    public BankAccount getAccount(Player player) { return this.getAccount(player.getUUID()); }
    public BankAccount getAccount(UUID player)
    {
        if(this.playerBankData.containsKey(player))
            return this.playerBankData.get(player).account;
        //Create a new bank account for the player
        BankAccount newAccount = this.generateBankAccount(player);
        this.playerBankData.put(player, new BankDataEntry(newAccount, PlayerBankReference.of(player).flagAsClient(this)));
        this.markAccountDirty(player);
        return newAccount;
    }

    public boolean deleteAccount(UUID player)
    {
        if(this.isClient()) {
            LightmansCurrency.LogWarning("Cannot delete a bank account from the logical client!");
            return false;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server == null)
            return false;

        if(!this.playerBankData.containsKey(player))
            return false;
        //If the player whose bank account got deleted is online, delete and replace their bank account/data
        ServerPlayer onlinePlayer = server.getPlayerList().getPlayer(player);
        this.playerBankData.remove(player);
        //Player is online, so create a new bank account after deleting the old one
        if(onlinePlayer != null)
        {
            //Since it's going to create a new bank account this will also
            //mark said bank account as dirty and automatically send the sync packet
            this.getAccount(player);
            //Send sync packet for their new selected bank account
            this.syncSelectedAccount(onlinePlayer);
        }
        else //Player is NOT online, so don't re-create it after deletion
        {
            this.setChanged();
            //Send deleted packet to all connected clients
            this.sendSyncPacket(this.builder().setUUID("DeleteAccount",player));
        }
        return true;

    }

    public void markAccountDirty(UUID playerID)
    {
        this.setChanged();
        //Send update packet to all connected clients
        this.syncBankAccount(playerID);
    }

    private void syncBankAccount(UUID player) { this.syncBankAccount(player,null); }

    private void syncBankAccount(UUID player, @Nullable ServerPlayer target)
    {
        BankAccount account = this.getAccount(player);
        LazyPacketData.Builder packet = this.builder().setUUID("UpdateAccount",player).setCompound("Account",account.save());
        if(target == null)
            this.sendSyncPacket(packet);
        else
            this.sendSyncPacket(packet,target);
    }

    public BankReference getSelectedAccount(Player player)
    {
        if(this.playerBankData.containsKey(player.getUUID()))
        {
            BankReference account = this.playerBankData.get(player.getUUID()).selected;
            if(!account.allowedAccess(player))
            {
                LightmansCurrency.LogInfo(player.getName().getString() + " is no longer allowed to access their selected bank account. Switching back to their personal account.");
                account = PlayerBankReference.of(player).flagAsClient(this);
                this.setSelectedAccount(player,account);
            }
            return account;
        }
        //Generate default bank account for the player
        BankReference account = PlayerBankReference.of(player).flagAsClient(this);
        this.setSelectedAccount(player,account);
        return account;
    }

    public void setSelectedAccount(Player player, BankReference account)
    {
        if(account == null)
            return;
        if(this.isClient()) {
            if(!LightmansCurrency.getProxy().isSelf(player))
                return;
            new CPacketSelectBankAccount(account).send();
            return;
        }
        if(!account.allowedAccess(player))
        {
            LightmansCurrency.LogInfo("Player does not have access to the selected account. Canceling selection.");
            return;
        }
        BankDataEntry data = this.playerBankData.get(player.getUUID());
        if(data == null)
        {
            data = new BankDataEntry(this.generateBankAccount(player.getUUID()),null);
            this.syncBankAccount(player.getUUID());
        }
        data.selected = account;

        this.setChanged();
        if(player instanceof ServerPlayer sp)
            this.syncSelectedAccount(sp);
    }

    private void syncSelectedAccount(ServerPlayer player)
    {
        this.sendSyncPacket(this.builder().setUUID("UpdateSelected",player.getUUID()).setCompound("Selected",this.getSelectedAccount(player).save()),player);
    }

    @Override
    protected void parseSyncPacket(LazyPacketData message) {
        if(message.contains("ClearAccounts"))
            this.playerBankData.clear();
        if(message.contains("DeleteAccount"))
            this.playerBankData.remove(message.getUUID("DeleteAccount"));
        if(message.contains("UpdateAccount"))
        {
            UUID account = message.getUUID("UpdateAccount");
            BankAccount ba = this.loadBankAccount(account,message.getNBT("Account")).flagAsClient(this);
            BankDataEntry data = this.playerBankData.containsKey(account) ? this.playerBankData.get(account) : new BankDataEntry(null,PlayerBankReference.of(account).flagAsClient(this));
            data.account = ba;
            this.playerBankData.put(account,data);
        }
        if(message.contains("UpdateSelected"))
        {
            UUID account = message.getUUID("UpdateSelected");
            BankReference selected = BankReference.load(message.getNBT("Selected")).flagAsClient(this);
            BankDataEntry data = this.playerBankData.containsKey(account) ? this.playerBankData.get(account) : new BankDataEntry(this.generateBankAccount(account).flagAsClient(this),null);
            data.selected = selected;
            this.playerBankData.put(account,data);
        }
    }

    @Override
    public void onPlayerJoin(ServerPlayer player) {
        //Sync all bank accounts
        for(UUID p : this.playerBankData.keySet())
            this.syncBankAccount(p,player);
        //Force their personal account to exist after initial sync so that it's not sent twice
        this.getAccount(player);
        //Sync their selected bank account
        this.syncSelectedAccount(player);
    }

    public int interestTick()
    {
        this.setChanged();
        return this.interestTick++;
    }

    public void resetInterestTick()
    {
        this.interestTick = 0;
        this.setChanged();
    }

    private static class BankDataEntry
    {
        BankAccount account;
        BankReference selected;
        BankDataEntry(BankAccount account,BankReference selected) { this.account = account; this.selected = selected; }
    }

}