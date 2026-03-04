package io.github.lightman314.lightmanscurrency.common.traders.search_filters;

import io.github.lightman314.lightmanscurrency.api.traders.terminal.IBasicTraderFilter;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.PendingSearch;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.builtin.BasicSearchFilter;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.trade.AuctionTradeData;
import net.minecraft.core.HolderLookup;

public class AuctionSearchFilter implements IBasicTraderFilter {

    @Override
    public void filterTrade(TradeData data, PendingSearch search, HolderLookup.Provider lookup) {
        if(data instanceof AuctionTradeData auction)
        {
            if(auction.isActive())
            {
                search.processFilter(ItemTraderSearchFilter.ITEM,ItemTraderSearchFilter.filterItems(auction.getAuctionItems(),lookup));
                search.processFilter(BasicSearchFilter.TOOLTIP,ItemTraderSearchFilter.filterItemTooltips(auction.getAuctionItems(),lookup));
            }
        }
    }

}
