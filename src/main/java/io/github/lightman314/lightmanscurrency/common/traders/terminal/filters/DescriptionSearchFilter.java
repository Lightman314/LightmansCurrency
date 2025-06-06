package io.github.lightman314.lightmanscurrency.common.traders.terminal.filters;

import io.github.lightman314.lightmanscurrency.api.traders.terminal.IBasicTraderFilter;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.PendingSearch;
import io.github.lightman314.lightmanscurrency.api.traders.trade.IDescriptionTrade;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import net.minecraft.core.HolderLookup;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class DescriptionSearchFilter implements IBasicTraderFilter {

    public static final String DESCRIPTION = "description";
    public static final String TOOLTIP = "tooltip";

    @Override
    public void filterTrade(TradeData data, PendingSearch search, HolderLookup.Provider lookup) {
        if(data instanceof IDescriptionTrade trade)
        {
            search.processFilter(DESCRIPTION,trade.getDescription().toLowerCase()::contains);
            search.processFilter(TOOLTIP,trade.getDescription().toLowerCase()::contains);
        }
    }

}
