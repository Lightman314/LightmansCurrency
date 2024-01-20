package io.github.lightman314.lightmanscurrency.api.traders.trade;

/**
 * While no functionality is strictly added, this interface must be inherited by the TradeData class
 * in order for it to appear on the search results when searching for barter trades via the LDI integration.
 */
public interface IBarterTrade {

	boolean isBarter();
	
}
