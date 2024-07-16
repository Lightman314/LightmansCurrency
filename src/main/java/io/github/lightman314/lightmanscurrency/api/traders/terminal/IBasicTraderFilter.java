package io.github.lightman314.lightmanscurrency.api.traders.terminal;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;

import javax.annotation.Nonnull;

public interface IBasicTraderFilter extends ITraderSearchFilter, ITradeSearchFilter {

    @Override
    default boolean filter(@Nonnull TraderData data, @Nonnull String searchText)
    {
        for(TradeData trade : data.getTradeData())
        {
            if(this.filterTrade(trade, searchText))
                return true;
        }
        return false;
    }
}