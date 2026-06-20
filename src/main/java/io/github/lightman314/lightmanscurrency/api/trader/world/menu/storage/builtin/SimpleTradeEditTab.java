package io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeSet;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeEditContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlotType;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import net.minecraft.resources.Identifier;
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
    protected boolean processTradeClick(TradeData trade, TradeSlotType type, int slotIndex, int mouseButton, ItemStack heldItem, TradeEditContext context) {
        return trade.processTradeClick(type,slotIndex,mouseButton,heldItem,context);
    }

    @Override
    protected boolean processTradeScroll(TradeData trade, TradeSlotType type, int slotIndex, float deltaY, ItemStack heldItem, TradeEditContext context) {
        return trade.processTradeScroll(type,slotIndex,deltaY,heldItem,context);
    }

    @Override
    public Identifier getKey() { return KEY; }
    @Override
    protected boolean isDefaultTab() { return true; }
    @Override
    public boolean canOpen() { return true; }
    @Override
    public int getTabSortPriority() { return Integer.MIN_VALUE; }

}
