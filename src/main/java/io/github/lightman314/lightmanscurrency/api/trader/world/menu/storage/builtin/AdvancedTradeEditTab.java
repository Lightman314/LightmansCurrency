package io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeSet;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeEditContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlotType;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class AdvancedTradeEditTab<T extends TradeData> extends TradeInteractionTab {

    private int nodeIndex = -1;
    private int tradeIndex = -1;
    protected int selection = -1;

    public AdvancedTradeEditTab(TraderStorageMenu menu) { super(menu); }

    @Override
    protected final List<TradeSet> editableTradeSets() {
        TraderData trader = this.getTrader();
        if(trader == null)
            return new ArrayList<>();
        List<TradingNode<?>> nodes = trader.getTradingNodes();
        if(this.nodeIndex >= 0 && this.nodeIndex < nodes.size())
            return Lists.newArrayList(new TradeSet(nodes.get(this.nodeIndex)).singleTrade(this.tradeIndex));
        return new ArrayList<>();
    }

    @Override
    protected boolean isSingleTrade() { return true; }

    @Nullable
    protected final TradeData getSelectedTrade()
    {
        List<TradeSet> set = this.editableTradeSets();
        if(set.isEmpty())
            return null;
        //Since the set is not empty, and it'll **always** have exactly 1 trade in the set as per
        return set.getFirst().getTrades().getFirst();
    }

    @Override
    protected boolean processTradeClick(TradeData trade,TradeSlotType type,int slotIndex,int mouseButton,ItemStack heldItem,TradeEditContext context) {
        return trade.processTradeClick(type,slotIndex,mouseButton,heldItem,context);
    }

    @Override
    protected boolean processTradeScroll(TradeData trade,TradeSlotType type,int slotIndex,float deltaY,ItemStack heldItem,TradeEditContext context) {
        return trade.processTradeScroll(type,slotIndex,deltaY,heldItem,context);
    }

    public static FancyPacketMap writeOpenMessage(int nodeIndex, int tradeIndex, int selection) {
        return FancyPacketMap.newMutable()
                .setInt("node",nodeIndex)
                .setInt("trade",tradeIndex)
                .setInt("selection",selection);
    }

    @Override
    public void onTabOpened(FancyPacketMap additional) {
        this.nodeIndex = additional.getInt("trade_set",-1);
        this.tradeIndex = additional.getInt("trade_index",-1);
        this.selection = additional.getInt("selection",-1);
    }

    //No sorting needed, as this tab should be hidden
    @Override
    public int getTabSortPriority() { return 0; }

}