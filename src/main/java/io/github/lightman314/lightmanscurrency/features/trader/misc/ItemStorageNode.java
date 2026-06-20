package io.github.lightman314.lightmanscurrency.features.trader.misc;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.StorageTabBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.UpgradeNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionUser;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IStorageMenuProvider;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.SimpleSyncedNode;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.BuiltInPermissions;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.upgrades.CapacityUpgradeType;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.api.upgrades.world.UpgradeStorage;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCUpgrades;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public class ItemStorageNode extends SimpleSyncedNode implements IPermissionUser, IStorageMenuProvider {

    private static final MapCodec<ItemStorageNode> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            CodecHelper.UNLIMITED_ITEM_LIST.fieldOf("storage").forGetter(ItemStorageNode::getContents)
    ).apply(builder,ItemStorageNode::new));

    public static final TraderNodeType<ItemStorageNode> TYPE = TraderNodeType.simple(ItemStorageNode::new,CODEC);

    private final FlexibleItemStorage storage = new FlexibleItemStorage(this::allowedInStorage,this::getStorageCapacity,this::onStorageChanged);
    public FlexibleItemStorage getStorage() { return this.storage; }

    private ItemStorageNode() {}
    private ItemStorageNode(List<ItemStack> contents) { this.storage.copyFrom(contents); }

    private List<ItemStack> getContents() { return this.storage.getContents(); }

    private void onStorageChanged(List<ItemStack> oldState,List<ItemStack> newState) {
        this.setChanged(builder -> builder
                .modifyMap("storage_update",this.storage.createPacket(oldState,newState)));
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    private boolean allowedInStorage(ItemStack stack) {
        for(IStorageFilterNode node : this.trader.getNodes(IStorageFilterNode.class))
        {
            if(node.itemAllowedInStorage(stack))
                return true;
        }
        return false;
    }

    public int getStorageCapacity() {
        int capacity = 64 * 9;
        UpgradeStorage upgradeStorage = this.trader.getNodeValue(UpgradeNode.TYPE,UpgradeNode::getStorage);
        if(upgradeStorage != null)
        {
            for(Pair<UpgradeType,ItemStack> upgrade : upgradeStorage)
            {
                if(upgrade.getFirst().is(LCUpgrades.ITEM_CAPACITY))
                {
                    int bonusCapacity = CapacityUpgradeType.getBonusCapacity(upgrade.getSecond());
                    if(bonusCapacity > 0)
                        capacity += bonusCapacity;
                }
            }
        }
        return capacity;
    }

    @Override
    public void createSyncPacket(FancyPacketMap.Mutable builder,ISyncingContext context) {
        //Simply send the entire list when making a fresh packet
        builder.setList("storage", LCFancyPacketTypes.ITEM_STACK,this.getContents());
    }

    @Override
    public void onDataSync(FancyPacketMap data) {
        if(data.contains("storage"))
            this.storage.copyFrom(data.getList("storage", LCFancyPacketTypes.ITEM_STACK));
        if(data.contains("storage_update"))
            this.storage.processPacket(data.getMap("storage_update"));
    }

    @Override
    public void addDefaultAllyPermission(Consumer<Permission<?>> handler) {
        handler.accept(BuiltInPermissions.OPEN_STORAGE);
    }

    @Override
    public void addTabs(StorageTabBuilder builder) {
        builder.addTab(ItemStorageTab::new);
    }

}
