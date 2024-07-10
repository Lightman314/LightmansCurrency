package io.github.lightman314.lightmanscurrency.common.traders.terminal.filters;

import io.github.lightman314.lightmanscurrency.api.traders.terminal.IBasicTraderFilter;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.ITradeSearchFilter;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class AuctionSearchFilter implements IBasicTraderFilter {

    @Override
    public boolean filterTrade(@Nonnull TradeData data, @Nonnull String searchText, @Nonnull HolderLookup.Provider lookup) {
        if(data instanceof AuctionTradeData auction)
        {
            if(auction.isActive())
            {
                for(ItemStack stack : auction.getAuctionItems())
                {
                    if(ITradeSearchFilter.filterItem(stack,searchText,lookup))
                        return true;
                }
            }
        }
        return false;
    }

}
