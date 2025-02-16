package io.github.lightman314.lightmanscurrency.api.traders.terminal;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface ITraderSearchFilter {

	@Deprecated(since = "2.2.4.3")
	default boolean filter(@Nonnull TraderData data, @Nonnull String searchText) { return false; }

	default void filter(TraderData data, PendingSearch search) { }
	
}
