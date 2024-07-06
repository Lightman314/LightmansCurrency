package io.github.lightman314.lightmanscurrency.api.traders.terminal;

import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;

import javax.annotation.Nonnull;

public interface ITradeSearchFilter {

    boolean filterTrade(@Nonnull TradeData data, @Nonnull String searchText);

}