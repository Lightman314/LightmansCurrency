package io.github.lightman314.lightmanscurrency.api.traders.terminal;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IBasicTraderFilter extends ITraderSearchFilter, ITradeSearchFilter {

    @Override
    @SuppressWarnings("deprecation")
    default boolean filter(TraderData data, String searchText, HolderLookup.Provider lookup)
    {
        for(TradeData trade : data.getTradeData())
        {
            if(this.filterTrade(trade, searchText,lookup))
                return true;
        }
        return false;
    }

    @Override
    default void filter(TraderData data, PendingSearch search, HolderLookup.Provider lookup) {
        for(TradeData trade : data.getTradeData())
            this.filterTrade(trade,search,lookup);
    }

}
