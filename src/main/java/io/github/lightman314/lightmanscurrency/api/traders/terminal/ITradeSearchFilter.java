package io.github.lightman314.lightmanscurrency.api.traders.terminal;

import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import net.minecraft.core.HolderLookup;

import javax.annotation.Nonnull;

public interface ITradeSearchFilter {

    boolean filterTrade(@Nonnull TradeData data, @Nonnull String searchText, @Nonnull HolderLookup.Provider lookup);

}
