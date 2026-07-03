package io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeSet;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeEditContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlot;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SimpleTradeEditTab extends TradeInteractionTab {

    public static final Identifier KEY = LCApi.id("simple_trade_edit");

    public SimpleTradeEditTab(TraderStorageMenu menu) { super(menu); }

    @Override
    protected List<TradeSet> editableTradeSets() {
        List<TradeSet> list = new ArrayList<>();
        TraderData trader = this.getTrader();
        if(trader == null)
            return list;
        for(TradingNode<?> node : trader.getTradingNodes())
            list.add(new TradeSet(node));
        return list;
    }

    @Override
    protected boolean processTradeClick(Player player,TradeData trade,TradeSlot slot, int mouseButton, ItemStack heldItem, TradeEditContext context) {
        return trade.processTradeClick(player,slot,mouseButton,heldItem,context);
    }

    @Override
    protected boolean processTradeScroll(Player player,TradeData trade,TradeSlot slot,float deltaY,ItemStack heldItem,TradeEditContext context) {
        //Don't process scroll interactions on the simple tab as there is a scroll bar that takes priority
        return false;
    }

    @Override
    public void openAdvancedEdit(TradeData trade,TradeSlot slot) {
        TraderData trader = this.getTrader();
        if(trader == null)
            return;
        TradingNode<?> node = trade.getHolder();
        if(node == null || node.advancedEditTabKey() == null)
            return;
        int nodeIndex = trader.getTradingNodes().indexOf(node);
        if(nodeIndex < 0)
            return;
        int tradeIndex = node.getTrades().indexOf(trade);
        if(tradeIndex < 0)
            return;
        this.getMenu().changeTab(node.advancedEditTabKey(),AdvancedTradeEditTab.writeOpenMessage(nodeIndex,tradeIndex,slot));
    }

    @Override
    public Identifier getKey() { return KEY; }
    @Override
    protected boolean isDefaultTab() { return true; }
    @Override
    public boolean canOpen() { return true; }
    @Override
    public int getTabSortPriority() { return Integer.MIN_VALUE; }

    @Override
    public boolean allowsScrollInteractions() { return false; }
    @Override
    public boolean isAdvancedEdit() { return false; }

}
