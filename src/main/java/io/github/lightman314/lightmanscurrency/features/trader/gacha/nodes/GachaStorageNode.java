package io.github.lightman314.lightmanscurrency.features.trader.gacha.nodes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.UpgradeNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionUser;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IStorageMenuTabProvider;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ITradeResourceProvider;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.SimpleSyncedNode;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.BuiltInResourceTypes;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.ResourceCollector;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.StorageTabBuilder;
import io.github.lightman314.lightmanscurrency.api.upgrades.CapacityUpgradeType;
import io.github.lightman314.lightmanscurrency.api.upgrades.world.UpgradeStorage;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCUpgrades;
import io.github.lightman314.lightmanscurrency.features.trader.item_common.LimitedItemStorage;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public class GachaStorageNode extends SimpleSyncedNode implements IPermissionUser, IStorageMenuTabProvider, ITradeResourceProvider {

    private static final MapCodec<GachaStorageNode> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            CodecHelper.UNLIMITED_ITEM_LIST.fieldOf("storage").forGetter(GachaStorageNode::getContents)
    ).apply(builder,GachaStorageNode::new));

    public static final TraderNodeType<GachaStorageNode> TYPE = TraderNodeType.simple(GachaStorageNode::new,CODEC);

    private final LimitedItemStorage storage = new LimitedItemStorage(this::getStorageCapacity,this::onStorageChanged);
    public LimitedItemStorage getStorage() { return this.storage; }

    private GachaStorageNode() {}
    private GachaStorageNode(List<ItemStack> contents) {
        this.storage.copyFrom(contents);
    }

    private List<ItemStack> getContents() { return this.storage.getContents(); }

    private void onStorageChanged(Consumer<FancyPacketMap.Mutable> packetWriter) {
        this.setChanged(builder -> builder.modifyMap("storage_update",packetWriter));
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    public int getStorageCapacity() {
        UpgradeStorage upgrades = this.getNodeValue(UpgradeNode.TYPE,UpgradeNode::getStorage);
        return CapacityUpgradeType.getTotalCapacity(64 * 9,upgrades,LCUpgrades.ITEM_CAPACITY);
    }

    @Override
    public void createSyncPacket(FancyPacketMap.Mutable builder, ISyncingContext context) {
        builder.setList("storage",LCFancyPacketTypes.ITEM_STACK,this.getContents());
    }

    @Override
    public void onDataSync(FancyPacketMap data) {
        if(data.contains("storage"))
            this.storage.copyFrom(data.getList("storage",LCFancyPacketTypes.ITEM_STACK));
        if(data.contains("storage_update"))
            this.storage.processPacket(data.getMap("storage_update"));
    }

    @Override
    public void addDefaultAllyPermission(Consumer<Permission<?>> handler) {
        handler.accept(LCPermissions.OPEN_STORAGE);
    }

    @Override
    public void addTabs(StorageTabBuilder builder) {
        //Gacha Storage Tab
    }

    @Override
    public void attachResource(ResourceCollector collector) {
        collector.addResource(BuiltInResourceTypes.ITEM,this.storage);
    }

}
