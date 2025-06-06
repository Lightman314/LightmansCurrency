package io.github.lightman314.lightmanscurrency.api.traders.terminal;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IBasicTraderFilter extends ITraderSearchFilter, ITradeSearchFilter {

    @Override
    default void filter(TraderData data, PendingSearch search) {
        for(TradeData trade : data.getTradeData())
            this.filterTrade(trade,search);
    }

}
