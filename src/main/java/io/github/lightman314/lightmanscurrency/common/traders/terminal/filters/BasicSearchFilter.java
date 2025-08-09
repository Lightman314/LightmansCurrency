package io.github.lightman314.lightmanscurrency.common.traders.terminal.filters;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.FilterUtils;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.ITraderSearchFilter;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.PendingSearch;
import net.minecraft.core.HolderLookup;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
public class BasicSearchFilter implements ITraderSearchFilter {

	public static final String TYPE = "type";
	public static final String OWNER = "owner";
	public static final String NAME = "name";
	public static final String ID = "id";
	public static final String TRADE_COUNT = "trades";
	public static final String CUSTOMER_READY = "ready";
	public static final String STOCK_COUNT = "stock";
	public static final String TRADE_STOCK = "tradeStock";

	@Override
	public void filter(TraderData data, PendingSearch search, HolderLookup.Provider lookup) {
		search.processFilter(TYPE,this.checkType(data));
		search.processFilter(OWNER,this.checkOwner(data));
		search.processFilter(NAME,this.checkName(data));
		FilterUtils.longRange(search,ID,data.getID());
		FilterUtils.intRange(search,TRADE_COUNT,data.validTradeCount());
		FilterUtils.boolCheck(search,CUSTOMER_READY,data.readyForCustomers());
		FilterUtils.checkStockCount(search,data);
	}

	private Predicate<String> checkType(TraderData trader)
	{
		return type -> {
			//If namespace is provided, require that the traders id starts with the given type string
			if(type.contains(":"))
				return trader.type.type.toString().startsWith(type);
			else //Otherwise, simply check if the given string is part of the traders id
				return trader.type.type.toString().contains(type);
		};
	}

	private Predicate<String> checkOwner(TraderData data) {
		return owner -> data.getOwner().getName().getString().toLowerCase().contains(owner.toLowerCase());
	}

	private Predicate<String> checkName(TraderData data) {
		return name -> data.getName().getString().toLowerCase().contains(name.toLowerCase());
	}

}
