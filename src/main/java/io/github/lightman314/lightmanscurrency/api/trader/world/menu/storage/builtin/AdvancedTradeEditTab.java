package io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.BuiltInPermissions;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeSet;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeEditContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlot;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceType;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.PreviousTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.rules.TradeTradeRuleTab;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class AdvancedTradeEditTab extends TradeInteractionTab {

    private int nodeIndex = -1;
    private int tradeIndex = -1;
    protected TradeSlot selection = TradeSlot.NONE;
    @Override
    public TradeSlot getSelectedSlot() { return this.selection; }

    private BiConsumer<TradeSlot,TradeSlot> selectionListener = (o,n) -> {};

    public AdvancedTradeEditTab(TraderStorageMenu menu) { super(menu); }

    public void withSelectionListener(BiConsumer<TradeSlot,TradeSlot> selectionListener) { this.selectionListener = selectionListener; }

    @Override
    public boolean canOpen() { return this.getPermission(BuiltInPermissions.EDIT_TRADES); }

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
    public final TradeData getSelectedTrade()
    {
        List<TradeSet> set = this.editableTradeSets();
        if(set.isEmpty())
            return null;
        //Since the set is not empty, and it'll **always** have exactly 1 trade in the set as per
        return set.getFirst().getTrades().getFirst();
    }

    @Override
    protected boolean processTradeClick(Player player,TradeData trade,TradeSlot slot,int mouseButton,ItemStack heldItem,TradeEditContext context) {
        return trade.processTradeClick(player,slot,mouseButton,heldItem,context);
    }

    @Override
    protected boolean processTradeScroll(Player player,TradeData trade,TradeSlot slot,float deltaY,ItemStack heldItem,TradeEditContext context) {
        return trade.processTradeScroll(player,slot,deltaY,heldItem,context);
    }

    public static FancyPacketMap writeOpenMessage(int nodeIndex,int tradeIndex,TradeSlot slot) {
        return FancyPacketMap.map()
                .setInt("trade_set",nodeIndex)
                .setInt("trade_index",tradeIndex)
                .setMap("selection",FancyPacketMap.map().action(slot.encode()));
    }

    @Override
    public boolean isAdvancedEdit() { return true; }

    @Override
    public boolean isSelected(TradeSlot slot) { return this.selection.equals(slot); }

    @Override
    public void changeSelection(TradeSlot slot) {
        if(this.selection.equals(slot))
            return;
        TradeSlot oldSlot = this.selection;
        this.selection = slot;
        this.selectionListener.accept(oldSlot,this.selection);
    }

    public final void setTradePriceType(TradePriceType<?> type) {
        TradeData trade = this.getSelectedTrade();
        TraderData trader = this.getTrader();
        if(trade != null && trader != null && trade.getInternalPrice().getType() != type)
        {
            TradePrice newPrice = type.factory().get();
            if(newPrice.currentlySupportsTrade(trader,trade))
                trade.setPrice(newPrice);
        }
        if(this.isClient())
            this.sendToServer(FancyPacketMap.map().setRegistryEntry("changePriceType",LCRegistries.Trader.TRADE_PRICE_TYPE,type));
    }

    public final void validatePriceType() {
        TraderData trader = this.getTrader();
        TradeData trade = this.getSelectedTrade();
        if(trader != null && trade != null) {
            TradePriceType<?> oldType = trade.getInternalPrice().getType();
            trade.validatePrice(trader);
            this.sendToSelf(FancyPacketMap.flag("price_type_flag"));
        }
    }

    @Override
    public void onTabOpened(FancyPacketMap additional) {
        this.nodeIndex = additional.getInt("trade_set",-1);
        this.tradeIndex = additional.getInt("trade_index",-1);
        if(additional.contains("selection"))
            this.selection = TradeSlot.decode(additional.getMap("selection"));
        else
            this.selection = TradeSlot.NONE;
    }

    public final void openTradeRuleTab() {
        if(this.getPermission(BuiltInPermissions.EDIT_TRADE_RULES))
            TradeTradeRuleTab.open(this.getMenu(),this.nodeIndex,this.tradeIndex,new PreviousTab(this,writeOpenMessage(this.nodeIndex,this.tradeIndex,this.selection)));
    }

    //No sorting needed, as this tab should be hidden
    @Override
    public int getTabSortPriority() { return 0; }

    @Override
    public boolean allowsScrollInteractions() { return true; }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void handleMessage(FancyPacketMap packet) {
        super.handleMessage(packet);
        if(packet.contains("changePriceType"))
            this.setTradePriceType(packet.getRegistryEntry("changePriceType",LCRegistries.Trader.TRADE_PRICE_TYPE));
    }
}