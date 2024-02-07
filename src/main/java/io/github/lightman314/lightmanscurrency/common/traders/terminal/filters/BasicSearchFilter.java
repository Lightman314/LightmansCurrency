package io.github.lightman314.lightmanscurrency.common.traders.terminal.filters;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.ITraderSearchFilter;

public class BasicSearchFilter implements ITraderSearchFilter {

	@Override
	public boolean filter(TraderData data, String searchText) {
		if(data.getOwner().getOwnerName(true).toLowerCase().contains(searchText.toLowerCase()))
			return true;
		if(data.getName().getString().toLowerCase().contains(searchText.toLowerCase()))
			return true;
		return false;
	}

}
