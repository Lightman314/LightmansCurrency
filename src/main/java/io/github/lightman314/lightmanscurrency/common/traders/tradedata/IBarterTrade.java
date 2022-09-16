package io.github.lightman314.lightmanscurrency.common.traders.tradedata;

/**
 * While no functionality is strictly added, this interface must be inherited by the TradeData class
 * in order for it to appear on the search results when searching for barter trades.
 */
public interface IBarterTrade {

	public boolean isBarter();
	
}
