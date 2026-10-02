package io.github.lightman314.lightmanscurrency.api.trader.nodes.templates;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.keys.DualKey;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeHelper;
import io.github.lightman314.lightmanscurrency.api.money.MoneyStats;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.TraderStats;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IAdminSettingProvider;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ISettingsStorageIONode;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.OutOfStockNotification;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleHolder;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.ISettingsStorageIO;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin.MoneyReceipt;
import io.github.lightman314.lightmanscurrency.api.trader.trade.settings.SettingsIOTrade;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.StorageTabBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionUser;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IStorageMenuTabProvider;
import io.github.lightman314.lightmanscurrency.api.trader.event.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeFailedException;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeResult;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.SimpleTradeEditTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.rules.TradeTradeRuleTab;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class TradingNode<T extends TradeData> extends PlayerSyncedTraderNode implements IStorageMenuTabProvider, IPermissionUser, ISettingsStorageIONode {

    protected TradingNode() {}

    public static final TextEntry VALUE_TRADE = TextEntry.traderNodeValue(LCApi.id("trades"),"trade");
    public static final TextEntry VALUE_TRADE_RULES = TextEntry.traderNodeValue(LCApi.id("trades"),"trade_rules");
    public static final TextEntry VALUE_TRADE_COUNT = TextEntry.traderNodeValue(LCApi.id("trades"),"trade_count");

    public static final TextEntry TOOLTIP_TERMINAL_TRADE_COUNT = TextEntry.tooltip(LCApi.MODID,"terminal.info.trade_count");
    public static final TextEntry TOOLTIP_TERMINAL_OUT_OF_STOCK_COUNT = TextEntry.tooltip(LCApi.MODID,"terminal.info.trade_count.out_of_stock");

    //By default, sort by hash code so that two trading nodes aren't accidentally
    public int getPriority() { return this.getType().hashCode(); }

    public int getTradeCount() { return this.getTrades().size(); }
    @Nullable
    public T getTrade(int tradeIndex) {
        List<T> trades = this.getTrades();
        if(tradeIndex < 0 || tradeIndex >= trades.size())
            return null;
        return trades.get(tradeIndex);
    }
    protected final int getGlobalTradeIndex(int tradeIndex) {
        int offset = 0;
        for(TradingNode<?> node : this.getTradingNodes()) {
            if(node != this)
                offset += node.getTradeCount();
            else
                return offset + tradeIndex;
        }
        return offset + tradeIndex;
    }
    protected final int getTradeIndex(TradeData trade) {
        try {return this.getMutableTrades().indexOf((T)trade);
        } catch (ClassCastException ignored) { return -1; }
    }
    protected final T createAndInitializeTrade() {
        T trade = this.createNewTrade();
        trade.initialize(this);
        return trade;
    }
    protected abstract T createNewTrade();
    protected abstract List<T> getMutableTrades();
    public List<T> getTrades() { return ImmutableList.copyOf(this.getMutableTrades()); }

    @Nullable
    public abstract Identifier advancedEditTabKey();

    public final void setTradeChanged(TradeData trade,Consumer<FancyPacketMap.Mutable> packet,Predicate<ISyncingContext> filter) {
        int tradeIndex = this.getTradeIndex(trade);
        //Trade is no longer present in this node
        if(tradeIndex < 0)
            return;
        this.setChanged(b ->
            b.modifyListEntry("trades",LCFancyPacketTypes.MAP,tradeIndex,m -> {
                FancyPacketMap.Mutable builder = m.mutable();
                packet.accept(builder);
                //If a change happened, clearly it wasn't removed so delete the remove flag
                builder.remove("remove");
                return builder.immutable();
            },() -> FancyPacketMap.EMPTY),filter);
    }

    protected final void setTradeRemoved(int tradeIndex) {
        if(tradeIndex < 0)
            return;
        this.setChanged(b ->
                b.modifyListEntry("trades",LCFancyPacketTypes.MAP,tradeIndex,m -> {
                    FancyPacketMap.Mutable builder = m.mutable();
                    builder.clear() //Clear any existing changes, and then set the "remove" flag
                            .setFlag("remove");
                    return builder;
                },() -> FancyPacketMap.EMPTY));
    }

    protected final void attachTrades() {
        for(TradeData trade : this.getMutableTrades())
            trade.initialize(this);
    }

    public abstract Component getSetLabel();

    @Override
    public void createSyncPacket(FancyPacketMap.Mutable builder, ISyncingContext context) {
        List<FancyPacketMap> tradeList = new ArrayList<>();
        for(TradeData trade : this.getTrades())
        {
            FancyPacketMap.Mutable entry = FancyPacketMap.map();
            trade.getFullPacket(entry,context);
            tradeList.add(entry.immutable());
        }
        builder.setList("trades",LCFancyPacketTypes.MAP,tradeList);
    }

    @Override
    public void onDataSync(FancyPacketMap data) {
        if(data.contains("trades"))
        {
            List<T> trades = this.getMutableTrades();
            List<FancyPacketMap> tradeList = data.getList("trades",LCFancyPacketTypes.MAP);
            for(int i = 0; i < tradeList.size(); ++i)
            {
                FancyPacketMap entry = tradeList.get(i);
                if(entry.contains("remove"))
                {
                    while(tradeList.size() > i)
                        tradeList.removeLast();
                    //Exit the loop, since presumably everything past this is now also removed
                    break;
                }
                else
                {
                    //If it's not a removal, then force the entry to exist
                    while(trades.size() <= i)
                        trades.add(this.createAndInitializeTrade());
                    if(!entry.isEmpty())
                        trades.get(i).handlePacket(entry);
                }
            }
        }
    }

    protected final void forceTradeCount(int newCount) {
        List<T> trades = this.getMutableTrades();
        //End early if the size already matches
        if(newCount == trades.size())
            return;
        while(trades.size() > newCount)
        {
            trades.removeLast();
            this.setTradeRemoved(trades.size());
        }
        while(trades.size() < newCount)
        {
            T newTrade = this.createAndInitializeTrade();
            trades.add(newTrade);
            this.setTradeChanged(newTrade,p -> {},Predicates.alwaysTrue());
        }
    }

    public abstract TradeResult executeTrade(TradeContext context, int tradeIndex) throws TradeFailedException;

    @ApiStatus.Internal
    public final TradeEvent.Pre runPreTradeEvent(TradeContext context,int tradeIndex)
    {
        TradeData trade = this.getTrade(tradeIndex);
        TradeEvent.Pre event = new TradeEvent.Pre(context,this,trade);
        if(trade == null)
        {
            event.setCanceled(true);
            return event;
        }
        //Push to trade rules
        TradeRule.beforeTrade(this,trade,event);
        //Push to public event bus for external modifications
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    protected final void postOutOfStockNotification(int tradeIndex) {
        this.postNotification(new OutOfStockNotification(this.getTrader(),this.getGlobalTradeIndex(tradeIndex)));
    }

    protected final TradeResult finishSuccessfulTrade(TradeContext context,TradeData trade,TradePrice pricePaid,TradePrice.TransferResult priceResult) { return this.finishSuccessfulTrade(context,trade,pricePaid,priceResult,ImmutableList.of()); }
    protected final TradeResult finishSuccessfulTrade(TradeContext context,TradeData trade,TradePrice pricePaid,TradePrice.TransferResult priceResult,@Nullable Notification notification) { return this.finishSuccessfulTrade(context,trade,pricePaid,priceResult,ImmutableList.of(),notification); }
    protected final TradeResult finishSuccessfulTrade(TradeContext context,TradeData trade,TradePrice pricePaid,TradePrice.TransferResult priceResult,List<?> product) { return this.finishSuccessfulTrade(context,trade,pricePaid,priceResult,product,null); }
    protected final TradeResult finishSuccessfulTrade(TradeContext context,TradeData trade,TradePrice pricePaid,TradePrice.TransferResult priceResult, List<?> product,@Nullable Notification notification)
    {
        //Commit the actions performed within the trades context
        context.commit();
        //Automatically increment all trade-related statistics
        this.addToStat(TraderStats.INTERACTION_COUNT,1);
        this.addToStat(TraderStats.LAST_INTERACTION,TimeHelper.getCurrentTime());
        if(priceResult.getReceipt() instanceof MoneyReceipt mr) {
            MoneyValue amount = mr.getMoney();
            if(trade.isSale())
                this.addToStat(MoneyStats.MONEY_EARNED,amount);
            else if(trade.isPurchase())
                this.addToStat(MoneyStats.MONEY_PAID,amount);
        }
        MoneyValue taxesPaid = priceResult.getTaxesPaid();
        if(!taxesPaid.isEmpty())
            this.addToStat(MoneyStats.TAXES_PAID,taxesPaid);
        //Push the post-trade event
        TradeEvent.Post event = new TradeEvent.Post(context,this,trade,pricePaid,priceResult,product,Optional.ofNullable(notification));
        TradeRule.afterTrade(this,trade,event);
        NeoForge.EVENT_BUS.post(event);
        return TradeResult.success(event);
    }

    @Override
    public void addDefaultAllyPermission(Consumer<Permission<?>> handler) {
        handler.accept(LCPermissions.EDIT_TRADES);
    }

    @Override
    public void addTabs(StorageTabBuilder builder) {
        builder.addTab(SimpleTradeEditTab::new);
        if(this.getTrade(0) instanceof TradeRuleHolder)
            builder.addTab(TradeTradeRuleTab::new);
    }

    public int getCustomerReadyTrades() {
        return (int)this.getTrades().stream().filter(TradeData::isCustomerReady).count();
    }

    public int getTradesInStock(@Nullable TransactionContext transaction) {
        TraderData trader = this.getTrader();
        if(trader == null)
            return 0;
        int count = 0;
        for(TradeData trade : this.getTrades()) {
            if(trade.isCustomerReady() && trade.predictHasStock(trader,transaction))
                count++;
        }
        return count;
    }

    protected final Optional<Integer> getTerminalTradeColor() {
        int customerReadyTrades = this.getCustomerReadyTrades();
        //Red if 0 customer-ready trades
        if(customerReadyTrades <= 0)
            return Optional.of(ARGB.opaque(ChatFormatting.RED.getColor()));
        //Green if creative/inifinite stock
        if(IAdminSettingProvider.hasInfiniteStock(this))
            return Optional.of(ARGB.opaque(ChatFormatting.GREEN.getColor()));
        //Orange if all trades are out of stock
        int tradesInStock = this.getTradesInStock(null);
        if(tradesInStock <= 0)
            return Optional.of(ARGB.opaque(ChatFormatting.GOLD.getColor()));
        return Optional.empty();
    }

    protected final void appendTerminalTradeStatus(Consumer<Component> builder) {
        int customerReadyTrades = this.getCustomerReadyTrades();
        if(customerReadyTrades > 0) {
            builder.accept(TOOLTIP_TERMINAL_TRADE_COUNT.get(customerReadyTrades));
            int outOfStock = customerReadyTrades - this.getTradesInStock(null);
            if(outOfStock > 0)
                builder.accept(TOOLTIP_TERMINAL_OUT_OF_STOCK_COUNT.get(outOfStock));
        }
    }

    @Override
    public List<ISettingsStorageIO> getEntries(Player player) {
        List<ISettingsStorageIO> result = new ArrayList<>();
        result.add(this);
        DualKey key = this.getSettingsKey();
        for(int i = 0; i < this.getTradeCount(); ++i) {
            TradeData trade = this.getTrade(i);
            SettingsIOTrade.getSettingsForTrade(trade,key,i,result::add);
        }
        return result;
    }

}