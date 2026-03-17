package io.github.lightman314.lightmanscurrency.api.traders.terminal;

import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import net.minecraft.core.HolderLookup;

public interface ITraderSearchFilter {

	void filter(TraderData data, PendingSearch search, HolderLookup.Provider lookup);

}
