package io.github.lightman314.lightmanscurrency.common.traders.auction.nodes;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.PlayerSyncedTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;

import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IPermissionProvider;
import io.github.lightman314.lightmanscurrency.common.core.ModStats;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tabs.AuctionStorageTab;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionPlayerStorage;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuctionStorageNode extends PlayerSyncedTraderNode implements IPermissionProvider {

    public static final MapCodec<AuctionStorageNode> MAP_CODEC = AuctionPlayerStorage.SET_CODEC.fieldOf("storage").xmap(
            AuctionStorageNode::new,n -> n.storage);

    public static final TraderNodeType<AuctionStorageNode> TYPE = TraderNodeType.simple(AuctionStorageNode::new,MAP_CODEC);

    private final Map<UUID,AuctionPlayerStorage> storage = new HashMap<>();

    private AuctionStorageNode() { }
    private AuctionStorageNode(Map<UUID,AuctionPlayerStorage> storage) {
        this.storage.putAll(storage);
        for(AuctionPlayerStorage entry : this.storage.values())
            entry.withListener(() -> this.setStorageChanged(entry.getOwner()));
    }

    @Override
    public boolean isStorageOnly() { return true; }
    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    public void awardAuctionWinStat(PlayerReference lastBidder)
    {
        Player player = lastBidder.getPlayer();
        if(player != null)
            player.awardStat(ModStats.STAT_AUCTION_WINS);
        else
        {
            AuctionPlayerStorage storage = this.getStorage(player);
            storage.pendingWinStats++;
            this.setChangedNoPacket();
        }
    }

    public AuctionPlayerStorage getStorage(Player player) { return getStorage(PlayerReference.of(player)); }
    public AuctionPlayerStorage getStorage(PlayerReference player) {
        if(player == null)
            return null;
        if(!this.storage.containsKey(player.id))
        {
            //Create new storage entry for the player
            this.storage.put(player.id, new AuctionPlayerStorage(player));
            this.setStorageChanged(player);
        }
        return this.storage.get(player.id);
    }

    private void setStorageChanged(PlayerReference player)
    {
        if(this.isClient())
            return;
        this.setChanged(builder -> builder.setCustom("Storage",this.getStorage(player),ModLazyPackets.AUCTION_STORAGE),player);
    }

    @Override
    public void createSyncPacket(LazyPacketData.Builder builder,Player player) {
        builder.setCustom("storage",this.getStorage(player),ModLazyPackets.AUCTION_STORAGE);
    }

    @Override
    public void onDataSync(LazyPacketData data) {
        if(data.contains("storage"))
        {
            AuctionPlayerStorage storage = data.getCustom("storage",ModLazyPackets.AUCTION_STORAGE);
            this.storage.put(storage.getOwner().id,storage);
            storage.withListener(() -> this.setStorageChanged(storage.getOwner()));
        }
    }

    @Override
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
        DataContext<Tag> context = DataContext.createNBT(lookup);
        //Load storage
        if(tag.contains("StorageData"))
        {
            this.storage.clear();
            ListTag storageList = tag.getList("StorageData", Tag.TAG_COMPOUND);
            for(int i = 0; i < storageList.size(); ++i)
            {
                AuctionPlayerStorage storageEntry = AuctionPlayerStorage.load(storageList.getCompound(i),context);
                if(storageEntry.getOwner() != null)
                {
                    this.storage.put(storageEntry.getOwner().id, storageEntry);
                    storageEntry.withListener(() -> this.setStorageChanged(storageEntry.getOwner()));
                }
            }
        }
    }

    @Override
    public boolean blockPermission(String permission) { return !permission.equals(Permissions.OPEN_STORAGE); }

    @Override
    public int getPermissionLevel(String permission, PlayerReference player) {
        if(permission.equals(Permissions.OPEN_STORAGE) || permission.equals(Permissions.EDIT_TRADES))
            return 1;
        return 0;
    }

    @Override
    public void applyStorageTabs(ITraderStorageMenu menu) {
        menu.addTab(new AuctionStorageTab(menu));
    }

}
