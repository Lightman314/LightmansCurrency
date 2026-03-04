package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;

public interface ITradeListener {

    default void beforeTrade(TradeEvent.PreTradeEvent event) {}
    default void tradeCost(TradeEvent.TradeCostEvent event) {}
    default void afterTrade(TradeEvent.PostTradeEvent event) {}

}
