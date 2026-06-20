package io.github.lightman314.lightmanscurrency.features.api_impl;

import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.trader.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.INetworkController;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.features.api_impl.data.TraderDataCache;

import javax.annotation.Nullable;
import java.util.List;

public final class TraderAPIImpl implements TraderAPI {

    public static final TraderAPIImpl INSTANCE = new TraderAPIImpl();

    private TraderAPIImpl() {}

    @Override
    public boolean filterTrader(TraderData trader, String searchText) {
        return false;
    }

    @Override
    public List<TraderData> filterTraders(List<TraderData> traders, String searchText) {
        return List.of();
    }

    @Override
    public boolean filterTrade(TradeData trade, String searchText) {
        return false;
    }

    @Override
    public List<TradeData> filterTrades(List<TradeData> trades, String searchText) {
        return List.of();
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