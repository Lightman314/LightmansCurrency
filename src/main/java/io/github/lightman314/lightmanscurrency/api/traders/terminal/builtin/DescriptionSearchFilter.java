package io.github.lightman314.lightmanscurrency.api.traders.terminal.builtin;

import io.github.lightman314.lightmanscurrency.api.traders.terminal.IBasicTraderFilter;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.PendingSearch;
import io.github.lightman314.lightmanscurrency.api.traders.trade.IDescriptionTrade;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import net.minecraft.core.HolderLookup;

public class DescriptionSearchFilter implements IBasicTraderFilter {

    public static final String DESCRIPTION = "description";

    @Override
    public void filterTrade(TradeData data, PendingSearch search, HolderLookup.Provider lookup) {
        if(data instanceof IDescriptionTrade trade)
        {
            search.processFilter(DESCRIPTION,trade.getDescription().toLowerCase()::contains);
            //Going to let this re-use the "tooltip" search filter just because there's really no reason not to if I'll be honest
            search.processFilter(BasicSearchFilter.TOOLTIP,trade.getDescription().toLowerCase()::contains);
        }
    }

}
