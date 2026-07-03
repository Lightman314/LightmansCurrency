package io.github.lightman314.lightmanscurrency.api.trader.nodes.templates;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.StorageTabBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionUser;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IStorageMenuProvider;
import io.github.lightman314.lightmanscurrency.api.trader.event.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.BuiltInPermissions;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeFailedException;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeResult;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.SimpleTradeEditTab;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class TradingNode<T extends TradeData> extends PlayerSyncedTraderNode implements IStorageMenuProvider, IPermissionUser {

    //By default, sort by hash code so that two trading nodes aren't accidentally
    public int getPriority() { return this.getType().hashCode(); }

    public int getTradeCount() { return this.getTrades().size(); }
    @Nullable
    public T getTrade(int tradeIndex) {
        List<T> trades = this.getTrades();
        if(tradeIndex >= 0 && tradeIndex < trades.size())
            return trades.get(tradeIndex);
        return null;
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
            FancyPacketMap.Mutable entry = FancyPacketMap.newMutable();
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
        while(trades.size() >= newCount)
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
        //TODO push event to trade rules
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    protected final TradeResult finishSuccessfulTrade(TradeContext context,TradeData trade,TradePrice pricePaid,TradePrice.TransferResult priceResult) { return this.finishSuccessfulTrade(context,trade,pricePaid,priceResult,ImmutableList.of()); }
    protected final TradeResult finishSuccessfulTrade(TradeContext context,TradeData trade,TradePrice pricePaid,TradePrice.TransferResult priceResult,List<?> product)
    {
        //Commit the actions performed within the trades context
        context.commit();
        //Push the post-trade event
        TradeEvent.Post event = new TradeEvent.Post(context,this,trade,pricePaid,priceResult,product);
        //TODO push event to trade rules
        NeoForge.EVENT_BUS.post(event);
        return TradeResult.success(event);
    }

    @Override
    public void addDefaultAllyPermission(Consumer<Permission<?>> handler) {
        handler.accept(BuiltInPermissions.EDIT_TRADES);
    }

    @Override
    public void addTabs(StorageTabBuilder builder) {
        builder.addTab(SimpleTradeEditTab::new);
    }

}