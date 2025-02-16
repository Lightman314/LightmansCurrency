package io.github.lightman314.lightmanscurrency.common.traders.terminal.filters;

import io.github.lightman314.lightmanscurrency.api.traders.terminal.IBasicTraderFilter;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.PendingSearch;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import net.minecraft.core.HolderLookup;

import javax.annotation.Nonnull;

public class AuctionSearchFilter implements IBasicTraderFilter {

    @Override
    public void filterTrade(@Nonnull TradeData data, @Nonnull PendingSearch search, @Nonnull HolderLookup.Provider lookup) {
        if(data instanceof AuctionTradeData auction)
        {
            if(auction.isActive())
                search.processFilter(ItemTraderSearchFilter.ITEM,ItemTraderSearchFilter.filterItems(auction.getAuctionItems(),lookup));
        }
    }

}
