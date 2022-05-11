package io.github.lightman314.lightmanscurrency.discord.events;

import java.util.List;

import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.discord.listeners.CurrencyListener.SearchCategory;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData;
import net.minecraftforge.eventbus.api.Event;

/**
 * Called when the currency bot !search command is run.
 * Use to add results for other modded traders.
 */
public class DiscordTraderSearchEvent extends Event{
	
	private final UniversalTraderData trader;
	public final UniversalTraderData getTrader() { return this.trader; }
	private final String searchText;
	public final String getSearchText() { return this.searchText; }
	private final SearchCategory searchType;
	public final boolean filterByTrades() { return this.searchType.filterByTrade(); }
	public final boolean acceptTradeType(TradeData trade) { return this.searchType.acceptTradeType(trade); }
	public final boolean acceptTrader(ITrader trader) { return this.searchType.acceptTrader(trader, this.searchText); }
	private final List<String> output;
	
	public DiscordTraderSearchEvent(UniversalTraderData trader, String searchText, SearchCategory searchType, List<String> outputList)
	{
		this.trader = trader;
		this.searchText = searchText;
		this.searchType = searchType;
		this.output = outputList;
	}
	
	public void addToOutput(String line)
	{
		this.output.add(line);
	}
	
}
