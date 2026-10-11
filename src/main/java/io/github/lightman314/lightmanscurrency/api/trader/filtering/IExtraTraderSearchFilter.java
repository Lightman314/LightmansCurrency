package io.github.lightman314.lightmanscurrency.api.trader.filtering;

import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;

public interface IExtraTraderSearchFilter {

    void applySearchFilters(TraderData trader,PendingSearch search);

}
