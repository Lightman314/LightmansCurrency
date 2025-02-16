package io.github.lightman314.lightmanscurrency.api.traders.terminal;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import net.minecraft.core.HolderLookup;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface ITraderSearchFilter {

	@Deprecated(since = "2.2.4.3")
	default boolean filter(TraderData data, String searchText, HolderLookup.Provider lookup) { return false; }

	default void filter(TraderData data, PendingSearch search, HolderLookup.Provider lookup) { }

}
