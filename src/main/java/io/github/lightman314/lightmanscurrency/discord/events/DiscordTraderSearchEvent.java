package io.github.lightman314.lightmanscurrency.discord.events;

import java.util.List;

import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.discord.listeners.CurrencyListener.SearchCategory;
import net.minecraftforge.eventbus.api.Event;

/**
 * Called when the currency bot !search command is run.
 * Use to add results for other modded traders.
 */
public class DiscordTraderSearchEvent extends Event{
	
	private final TraderData trader;
	public final TraderData getTrader() { return this.trader; }
	private final String searchText;
	public final String getSearchText() { return this.searchText; }
	private final SearchCategory searchType;
	public final boolean filterByTrades() { return this.searchType.filterByTrade(); }
	public final boolean acceptTradeType(TradeData trade) { return this.searchType.acceptTradeType(trade); }
	public final boolean acceptTrader(TraderData trader) { return this.searchType.acceptTrader(trader, this.searchText); }
	private final List<String> output;
	
	public DiscordTraderSearchEvent(TraderData trader, String searchText, SearchCategory searchType, List<String> outputList)
	{
		this.trader = trader;
		this.searchText = searchText;
		this.searchType = searchType;
		this.output = outputList;
	}
	
	public void addToOutput(String line) { this.output.add(line); }
	
}
