package io.github.lightman314.lightmanscurrency.api.trader.filtering.interfaces;

import io.github.lightman314.lightmanscurrency.api.trader.filtering.PendingSearch;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;

public interface ISearchFilterPrice {

    void applySearchFilters(TradeData trade,PendingSearch search);

}
