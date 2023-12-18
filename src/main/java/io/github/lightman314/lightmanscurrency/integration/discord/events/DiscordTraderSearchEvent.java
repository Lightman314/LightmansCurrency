package io.github.lightman314.lightmanscurrency.integration.discord.events;

import java.util.List;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.integration.discord.listeners.CurrencyListener;
import net.minecraftforge.eventbus.api.Event;

/**
 * Called when the currency bot !search command is run.
 * Use to add results for other modded traders.
 * Called for every trader after confirming that the Trader is acceptable via the
 */
public class DiscordTraderSearchEvent extends Event{
	
	private final TraderData trader;
	public final TraderData getTrader() { return this.trader; }
	private final String searchText;
	public final String getSearchText() { return this.searchText; }
	private final CurrencyListener.SearchCategory searchType;
	public final boolean filterByTrades() { return this.searchType.filterByTrade(); }
	public final boolean acceptTradeType(TradeData trade) { return this.searchType.acceptTradeType(trade); }
	public final boolean acceptTrader(TraderData trader) { return this.searchType.acceptTrader(trader, this.searchText); }
	private final List<String> output;
	
	public DiscordTraderSearchEvent(TraderData trader, String searchText, CurrencyListener.SearchCategory searchType, List<String> outputList)
	{
		this.trader = trader;
		this.searchText = searchText;
		this.searchType = searchType;
		this.output = outputList;
	}
	
	public void addToOutput(String line) { this.output.add(line); }
	
}
