package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.SyncedTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.NodeCollector;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IUpgradeHandler;

import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeStackHandler;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin.UpgradesTab;
import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Range;

import javax.annotation.Nullable;

public class UpgradesNode extends SyncedTraderNode {

    private static final MapCodec<UpgradesNode> MAP_CODEC = UpgradeStackHandler.CODEC
            .fieldOf("upgrades")
            .xmap(UpgradesNode::new,UpgradesNode::getContainer);

    public static final TraderNodeType<UpgradesNode> TYPE = TraderNodeType.advanced(UpgradesNode::factory,MAP_CODEC);

    private final UpgradeStackHandler container;
    public final UpgradeStackHandler getContainer() { return this.container; }

    private boolean addDefaultTab = false;

    private UpgradesNode(UpgradesNodeData data) {
        this.container = new UpgradeStackHandler(data.upgrades).withListener(this::setUpgradesChanged);
        this.addDefaultTab = data.addMenuTab;
    }
    private UpgradesNode(UpgradeStackHandler container) {
        this.container = container.withListener(this::setUpgradesChanged);
    }

    @Override
    public void updateArgument(@Nullable Object argument) {
        UpgradesNodeData data = UpgradesNodeData.parse(argument);
        this.container.forceResize(data.upgrades);
        this.addDefaultTab = data.addMenuTab;
    }

    public boolean quickInsertUpgrade(ItemStack stack)
    {
        if(stack.getItem() instanceof UpgradeItem upgrade)
        {
            int startingCount = stack.getCount();
            ItemStack result = ItemHandlerHelper.insertItem(this.container,stack,false);
            return result.getCount() < startingCount;
        }
        return false;
    }

    @Override
    public void onAttach() { this.container.setParent(this.trader); }

    private void setUpgradesChanged() {
        this.setChanged(builder -> builder.setList("upgrades",this.container.getStacks(),ModLazyPackets.ITEM_STACK));
        for(TraderNode node : this.trader.getNodeIterable())
        {
            if(node instanceof IUpgradeHandler handler)
                handler.afterUpgradesChanged(this.container);
        }
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(LazyPacketData.Builder builder,ISyncingContext context) {
        builder.setList("upgrades",this.container.getStacks(),ModLazyPackets.ITEM_STACK);
    }

    @Override
    public void onDataSync(LazyPacketData data) {
        if(data.contains("upgrades"))
            this.container.load(data.getList("upgrades",ModLazyPackets.ITEM_STACK));
    }

    @Override
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
        if(tag.contains("Upgrades"))
            this.container.safeLoad(tag,"Upgrades",DataContext.createNBT(lookup));
    }

    private static UpgradesNode factory(@Nullable Object argument) {
        UpgradesNodeData data = UpgradesNodeData.parse(argument);
        return new UpgradesNode(data);
    }

    @Override
    public void applyStorageTabs(ITraderStorageMenu menu) {
        //Add the built-in upgrades tab if we've been flagged to do so
        if(this.addDefaultTab)
            menu.addTab(new UpgradesTab(menu));
    }

    /**
     * Helper method for adding this node with the given number of slots
     * @param collector The {@link NodeCollector} to register the node to
     * @param slots The number of upgrade slots available for this trader.
     */
    public static void addNode(NodeCollector collector,@Range(from = 1,to = 27) int slots)
    {
        collector.addNode(UpgradesNode.TYPE,new UpgradesNodeData(MathUtil.clamp(slots,1,27),false));
    }

    /**
     * Helper method for adding this node with the given number of slots
     * @param collector The {@link NodeCollector} to register the node to
     * @param slots The number of upgrade slots available for this trader.
     * @param addDefaultTab Whether the built-in {@link UpgradesTab} should be added to the storage menu.<br>
     *                      Should be <code>false</code> if your traders custom storage tab already includes the upgrade slots.
     */
    public static void addNode(NodeCollector collector,@Range(from = 1,to = 27) int slots, boolean addDefaultTab)
    {
        collector.addNode(UpgradesNode.TYPE,new UpgradesNodeData(MathUtil.clamp(slots,1,27),addDefaultTab));
    }

    private record UpgradesNodeData(int upgrades,boolean addMenuTab)
    {
        static UpgradesNodeData parse(@Nullable Object argument)
        {
            if(argument instanceof UpgradesNodeData data)
                return data;
            if(argument instanceof Number num)
                return new UpgradesNodeData(MathUtil.clamp(num.intValue(),1,27),false);
            return new UpgradesNodeData(5,false);
        }
    }

}
