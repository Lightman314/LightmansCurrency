package io.github.lightman314.lightmanscurrency.api.trader.filtering.interfaces;

import io.github.lightman314.lightmanscurrency.api.trader.filtering.PendingSearch;

public interface ISearchFilterNode {

    void applySearchFilters(PendingSearch search);
    default boolean isReadyForCustomers() { return false; }

}
