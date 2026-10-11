package io.github.lightman314.lightmanscurrency.api.trader.filtering;

import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;

public interface IExtraTradeSearchFilter {

    void applySearchFilters(TradeData trade,PendingSearch search);

}