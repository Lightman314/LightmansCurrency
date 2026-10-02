package io.github.lightman314.lightmanscurrency.features.trader.item_common;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.resource.SidedResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.ownership.interfaces.IOwnerHolder;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.ExternalInteractionsNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.*;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.StorageTabBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.UpgradeNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.SimpleSyncedNode;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.BuiltInResourceTypes;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.ResourceCollector;
import io.github.lightman314.lightmanscurrency.api.upgrades.CapacityUpgradeType;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.api.upgrades.world.UpgradeStorage;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCUpgrades;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ItemStorageNode extends SimpleSyncedNode implements IPermissionUser, IStorageMenuTabProvider,ITradeResourceProvider,IUpgradeUser,ITraderDestructionListener {

    private static final MapCodec<ItemStorageNode> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            CodecHelper.UNLIMITED_ITEM_LIST.fieldOf("storage").forGetter(ItemStorageNode::getContents)
    ).apply(builder,ItemStorageNode::new));

    public static final TraderNodeType<ItemStorageNode> TYPE = TraderNodeType.simple(ItemStorageNode::new,CODEC);

    public static final TextEntry TOOLTIP_INPUT_SETTINGS = TextEntry.tooltip(LCApi.MODID,"trader.settings.input.items");

    private final FlexibleItemStorage storage = new FlexibleItemStorage(this::allowedInStorage,this::getStorageCapacity,this::onStorageChanged);
    public FlexibleItemStorage getStorage() { return this.storage; }

    private final Map<Direction,ResourceHandler<ItemResource>> sidedCapabilities = new HashMap<>();
    public ResourceHandler<ItemResource> getCapabilityForSide(Direction relativeSide) {
        return this.sidedCapabilities.computeIfAbsent(relativeSide,s -> new SidedResourceHandler.WithExtractionRule<>(this.storage, ExternalInteractionsNode.getSidedState(this,s),this::canExtractItem));
    }

    private ItemStorageNode() {}
    private ItemStorageNode(List<ItemStack> contents) { this.storage.copyFrom(contents); }

    private List<ItemStack> getContents() { return this.storage.getContents(); }

    private void onStorageChanged(Consumer<FancyPacketMap.Mutable> packetWriter) {
        this.setChanged(builder -> builder.modifyMap("storage_update",packetWriter));
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    private boolean allowedInStorage(ItemResource item) { return IItemStorageFilter.itemAllowedInStorage(this,item); }

    protected boolean canExtractItem(ItemResource item) { return !IItemStorageFilter.denyItemExtraction(this,item); }

    public int getStorageCapacity() {
        UpgradeStorage upgrades = this.getNodeValue(UpgradeNode.TYPE,UpgradeNode::getStorage);
        return CapacityUpgradeType.getTotalCapacity(64 * 9,upgrades,LCUpgrades.ITEM_CAPACITY);
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
        handler.accept(LCPermissions.OPEN_STORAGE);
    }

    @Override
    public void addTabs(StorageTabBuilder builder) {
        builder.addTab(ItemStorageTab::new);
    }

    @Override
    public void attachResource(ResourceCollector collector) {
        collector.addResource(BuiltInResourceTypes.ITEM,this.storage);
    }

    @Override
    public boolean allowUpgrade(UpgradeType type) { return type.is(LCUpgrades.ITEM_CAPACITY); }

    @Override
    public void onTraderDestroyed(IOwnerHolder owner, Consumer<ItemStack> itemSpawner, Optional<MoneyResourceHandler> playerMoney) {
        for(ItemStack s : this.storage.getContents()) {
            for(ItemStack i : ItemHelper.splitStack(s)) {
                itemSpawner.accept(i);
            }
        }
    }
}
