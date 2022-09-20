package io.github.lightman314.lightmanscurrency.common.traders.terminal.filters;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;

public abstract class TraderSearchFilter {

	private static final List<TraderSearchFilter> REGISTERED_FILTERS = new ArrayList<>();
	
	public static void addFilter(TraderSearchFilter filter)
	{
		if(filter != null)
			REGISTERED_FILTERS.add(filter);
	}
	
	public static boolean CheckFilters(TraderData data, String searchText)
	{
		for(TraderSearchFilter filter : REGISTERED_FILTERS)
		{
			try{
				if(filter.filter(data, searchText))
					return true;
			} catch(Throwable t) { LightmansCurrency.LogError("Error filtering traders: ", t); }
		}
		return false;
	}
	
	public static List<TraderData> FilterTraders(List<TraderData> data, String searchText)
	{
		if(searchText.isBlank())
			return data;
		List<TraderData> results = new ArrayList<>();
		for(TraderData trader : data)
		{
			if(CheckFilters(trader, searchText))
				results.add(trader);
		}
		return results;
	}
	
	public abstract boolean filter(TraderData data, String searchText);
	
}