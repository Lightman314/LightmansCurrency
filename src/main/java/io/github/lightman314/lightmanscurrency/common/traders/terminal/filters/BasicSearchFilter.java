package io.github.lightman314.lightmanscurrency.common.traders.terminal.filters;

import io.github.lightman314.lightmanscurrency.common.traders.TraderData;

public class BasicSearchFilter extends TraderSearchFilter {

	@Override
	public boolean filter(TraderData data, String searchText) {
		if(data.getOwner().getOwnerName().toLowerCase().contains(searchText.toLowerCase()))
			return true;
		if(data.getName().getString().toLowerCase().contains(searchText.toLowerCase()))
			return true;
		return false;
	}

}