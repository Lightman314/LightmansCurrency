package io.github.lightman314.lightmanscurrency.discord.events;

import java.util.List;

import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
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
	private final boolean findSales;
	public final boolean findSales() { return this.findSales; }
	private final boolean findPurchases;
	public final boolean findPurchases() { return this.findPurchases; }
	private final boolean findBarters;
	public final boolean findBarters() { return this.findBarters; }
	private final boolean findOwners;
	public final boolean findOwners() { return this.findOwners; }
	private final boolean findTraders;
	public final boolean findTraders() { return this.findTraders; }
	private final List<String> output;
	
	public DiscordTraderSearchEvent(UniversalTraderData trader, String searchText, boolean findSales, boolean findPurchases, boolean findBarters, boolean findOwners, boolean findTraders, List<String> outputList)
	{
		this.trader = trader;
		this.searchText = searchText;
		this.findSales = findSales;
		this.findPurchases = findPurchases;
		this.findBarters = findBarters;
		this.findOwners = findOwners;
		this.findTraders = findTraders;
		this.output = outputList;
	}
	
	public void addToOutput(String line)
	{
		this.output.add(line);
	}
	
}
