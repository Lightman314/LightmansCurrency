package io.github.lightman314.lightmanscurrency.common.universal_traders.traderSearching;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;

public abstract class TraderSearchFilter {

	private static final List<TraderSearchFilter> REGISTERED_FILTERS = new ArrayList<>();
	
	public static void addFilter(TraderSearchFilter filter)
	{
		if(filter != null)
			REGISTERED_FILTERS.add(filter);
	}
	
	public static boolean checkFilters(UniversalTraderData data, String searchText)
	{
		for(TraderSearchFilter filter : REGISTERED_FILTERS)
		{
			if(filter.filter(data, searchText))
				return true;
		}
		return false;
	}
	
	public abstract boolean filter(UniversalTraderData data, String searchText);
	
}
