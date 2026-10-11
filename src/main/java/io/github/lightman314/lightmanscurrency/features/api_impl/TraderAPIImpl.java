package io.github.lightman314.lightmanscurrency.features.api_impl;

import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.trader.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.filtering.IExtraTradeSearchFilter;
import io.github.lightman314.lightmanscurrency.api.trader.filtering.IExtraTraderSearchFilter;
import io.github.lightman314.lightmanscurrency.api.trader.filtering.PendingSearch;
import io.github.lightman314.lightmanscurrency.api.trader.filtering.builtin.GlobalFilter;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.INetworkController;
import io.github.lightman314.lightmanscurrency.api.trader.filtering.interfaces.ISearchFilterNode;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.filtering.interfaces.ISearchFilterTrade;
import io.github.lightman314.lightmanscurrency.features.api_impl.data.TraderDataCache;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class TraderAPIImpl implements TraderAPI {

    public static final TraderAPIImpl INSTANCE = new TraderAPIImpl();

    private final List<IExtraTraderSearchFilter> extraTraderFilters = new ArrayList<>();
    private final List<IExtraTradeSearchFilter> extraTradeFilters = new ArrayList<>();

    private TraderAPIImpl() { this.registerExtraSearchFilter(GlobalFilter.INSTANCE); }

    @Override
    public void registerExtraTraderSearchFilter(IExtraTraderSearchFilter filter) { this.extraTraderFilters.add(filter); }
    @Override
    public void registerExtraTradeSearchFilter(IExtraTradeSearchFilter filter) { this.extraTradeFilters.add(filter); }

    @Override
    public boolean filterTrader(TraderData trader, String searchText,@Nullable TransactionContext transaction) {
        return this.filterTrader(trader,PendingSearch.of(searchText,transaction));
    }

    @Override
    public List<TraderData> filterTraders(List<TraderData> traders, String searchText,@Nullable TransactionContext transaction) {
        PendingSearch search = PendingSearch.of(searchText,transaction);
        List<TraderData> result = new ArrayList<>();
        for(TraderData trader : traders) {
            if(filterTrader(trader,search.copy()))
                result.add(trader);
        }
        return result;
    }

    private boolean filterTrader(TraderData trader,PendingSearch search) {
        for(ISearchFilterNode node : trader.getNodes(ISearchFilterNode.class))
            node.applySearchFilters(search);
        for(IExtraTraderSearchFilter filter : this.extraTraderFilters)
            filter.applySearchFilters(trader,search);
        return search.hasPassed();
    }

    @Override
    public boolean filterTrade(TradeData trade,String searchText,@Nullable TransactionContext transaction) { return this.filterTrade(trade,PendingSearch.of(searchText,transaction)); }

    @Override
    public List<TradeData> filterTrades(List<TradeData> trades,String searchText,@Nullable TransactionContext transaction) {
        PendingSearch search = PendingSearch.of(searchText,transaction);
        List<TradeData> result = new ArrayList<>();
        for(TradeData trade : trades) {
            if(this.filterTrade(trade,search.copy()))
                result.add(trade);
        }
        return result;
    }

    private boolean filterTrade(TradeData trade,PendingSearch search) {
        this.applyTradeFilters(trade,search);
        return search.hasPassed();
    }

    @Override
    public void applyTradeFilters(TradeData trade, PendingSearch search) {
        if(trade instanceof ISearchFilterTrade filter)
            filter.applySearchFilters(search);
        for(IExtraTradeSearchFilter filter : this.extraTradeFilters)
            filter.applySearchFilters(trade,search);
    }

    @Nullable
    @Override
    public TraderData getTrader(ISidedContext context, long traderID) { return TraderDataCache.TYPE.get(context).getTrader(traderID); }

    @Override
    public List<TraderData> getAllTraders(ISidedContext context) { return TraderDataCache.TYPE.get(context).getAllTraders(); }

    @Override
    public List<TraderData> getAllNetworkTraders(ISidedContext context) {
        List<TraderData> traders = this.getAllTraders(context);
        //Remove if not visible on network
        traders.removeIf(t -> !INetworkController.visibleToNetwork(t));
        return traders;
    }

    @Override
    public long initializeTrader(TraderData newTrader) {
        TraderDataCache data = TraderDataCache.TYPE.get(ISidedContext.LOGICAL_SERVER);
        if(data == null)
            return -1;
        return data.registerTrader(newTrader);
    }

    @Override
    public void deleteTrader(long traderID) {
        TraderDataCache data = TraderDataCache.TYPE.get(ISidedContext.LOGICAL_SERVER);
        if(data == null)
            return;
        data.deleteTrader(traderID);
    }
}