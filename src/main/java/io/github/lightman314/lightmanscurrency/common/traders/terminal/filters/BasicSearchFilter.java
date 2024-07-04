package io.github.lightman314.lightmanscurrency.common.traders.terminal.filters;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.ITraderSearchFilter;
import net.minecraft.core.HolderLookup;

import javax.annotation.Nonnull;

public class BasicSearchFilter implements ITraderSearchFilter {

	@Override
	public boolean filter(@Nonnull TraderData data, @Nonnull String searchText, @Nonnull HolderLookup.Provider lookup) {
		if(data.getOwner().getName().getString().contains(searchText.toLowerCase()))
			return true;
		if(data.getName().getString().toLowerCase().contains(searchText.toLowerCase()))
			return true;
		return false;
	}

}
