package io.github.lightman314.lightmanscurrency.common.traders.terminal.filters;

import io.github.lightman314.lightmanscurrency.api.traders.terminal.IBasicTraderFilter;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.PendingSearch;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class AuctionSearchFilter implements IBasicTraderFilter {

    @Override
    public void filterTrade(TradeData data, PendingSearch search) {
        if(data instanceof AuctionTradeData auction)
        {
            if(auction.isActive())
            {
                search.processFilter(ItemTraderSearchFilter.ITEM,ItemTraderSearchFilter.filterItems(auction.getAuctionItems()));
                search.processFilter(BasicSearchFilter.TOOLTIP,ItemTraderSearchFilter.filterItemTooltips(auction.getAuctionItems()));
            }
        }
    }
}