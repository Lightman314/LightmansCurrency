package io.github.lightman314.lightmanscurrency.client.features.trader.item.block_entity;

import com.google.common.primitives.Ints;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.ItemPositionData;
import io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.ItemPositionSetManager;
import io.github.lightman314.lightmanscurrency.features.trader.item.TradeItem;
import io.github.lightman314.lightmanscurrency.features.trader.item.blocks.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.features.trader.item.nodes.AbstractItemTradesNode;
import io.github.lightman314.lightmanscurrency.features.trader.item.trade.ItemTradeData;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemTraderRenderState extends BlockEntityRenderState {

    public ItemPositionData positionData = ItemPositionData.EMPTY;
    public float partialTicks = 0f;
    public BlockState blockState = Blocks.AIR.defaultBlockState();
    private final List<TradeItemDisplay> tradeDisplay = new ArrayList<>();
    public List<TradeItemDisplay> getTradeDisplay() { return Collections.unmodifiableList(this.tradeDisplay); }
    public void clear() {
        this.positionData = ItemPositionData.EMPTY;
        this.partialTicks = 0f;
        this.blockState = Blocks.AIR.defaultBlockState();
        this.tradeDisplay.clear();
    }

    public int getPackedLight(int minBlockLight) {
        return LightCoordsUtil.pack(Math.max(minBlockLight,LightCoordsUtil.block(this.lightCoords)),LightCoordsUtil.sky(this.lightCoords));
    }

    public static class TradeItemDisplay {
        public int stock;
        private final List<ItemStackRenderState> saleItems = new ArrayList<>();
        public boolean isEmpty() { return this.stock <= 0 || this.saleItems.isEmpty(); }
        public List<ItemStackRenderState> getSaleItems() { return this.saleItems; }
    }

    public static void extractItemTraderRenderState(ItemTraderBlockEntity be,ItemTraderRenderState state,ItemModelResolver modelResolver,float partialTicks) {
        state.clear();
        //Don't even bother extracting the render state if the config won't let us display them
        if(LCConfig.CLIENT.itemRenderLimit.get() <= 0)
            return;
        //TODO check variant in the future
        state.blockState = be.getBlockState();
        if(state.blockState == null)
            state.blockState = Blocks.AIR.defaultBlockState();
        state.positionData = ItemPositionSetManager.getDataForBlock(state.blockState);
        state.partialTicks = partialTicks;
        TraderData trader = be.getTrader();
        if(trader == null)
            return;
        AbstractItemTradesNode<?> node = trader.getFirstNode(AbstractItemTradesNode.class);
        if(node == null)
            return;
        try(TradeContext context = TradeContext.builder(trader).build()) {
            for (ItemTradeData trade : node.getTrades()) {
                TradeItemDisplay display = new TradeItemDisplay();
                display.stock = Ints.saturatedCast(trade.getStock(context));
                if(display.stock > 0) {
                    //Only bother collecting the items if the stock count is greater than zero
                    for(TradeItem item : trade.getCombinedItems()) {
                        ItemStackRenderState itemState = new ItemStackRenderState();
                        modelResolver.appendItemLayers(itemState,item.getDisplayStack(context),ItemDisplayContext.FIXED,null,null,0);
                        display.saleItems.add(itemState);
                    }
                }
                state.tradeDisplay.add(display);
                //Leave the loop once we have collected the displayable number of trade items
                if(state.tradeDisplay.size() >= state.positionData.getEntryCount())
                    break;
            }
        }
        //LightmansCurrency.LogDebug("Collected " + state.tradeDisplay.size() + " valid trade display entries for the item trader at " + be.getBlockPos() + "!");
    }

}