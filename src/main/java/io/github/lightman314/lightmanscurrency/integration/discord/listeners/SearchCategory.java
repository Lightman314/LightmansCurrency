package io.github.lightman314.lightmanscurrency.integration.discord.listeners;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;

import java.util.function.BiFunction;
import java.util.function.Function;

public enum SearchCategory
{
    TRADE_SALE(trade -> trade.getTradeDirection() == TradeDirection.SALE),
    TRADE_PURCHASE(trade -> trade.getTradeDirection() == TradeDirection.PURCHASE),
    TRADE_BARTER(trade -> trade.getTradeDirection() == TradeDirection.BARTER),
    TRADE_ANY(trade -> true),

    TRADER_OWNER((trader,search) -> search.isEmpty() || trader.getOwner().getName().getString().toLowerCase().contains(search)),
    TRADER_NAME((trader,search) -> search.isEmpty() || trader.getName().getString().toLowerCase().contains(search)),
    TRADER_ANY((trader,search) -> true);

    private final boolean filterByTrade;
    public boolean filterByTrade() { return this.filterByTrade; }

    private final Function<TradeData,Boolean> tradeFilter;
    public boolean acceptTradeType(TradeData trade) { return this.tradeFilter.apply(trade); }

    private final BiFunction<TraderData,String,Boolean> acceptTrader;
    public boolean acceptTrader(TraderData trader, String searchText) { return this.acceptTrader.apply(trader, searchText); }

    SearchCategory(Function<TradeData,Boolean> tradeFilter) {
        this.filterByTrade = true;
        this.tradeFilter = tradeFilter;
        this.acceptTrader = (t,s) -> true;
    }

    SearchCategory(BiFunction<TraderData,String,Boolean> acceptTrader) {
        this.filterByTrade = false;
        this.tradeFilter = (t) -> true;
        this.acceptTrader = acceptTrader;
    }

}