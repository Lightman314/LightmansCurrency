package io.github.lightman314.lightmanscurrency.api.traders.terminal;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import net.minecraft.core.HolderLookup;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface ITraderSearchFilter {

	void filter(TraderData data, PendingSearch search, HolderLookup.Provider lookup);

}
