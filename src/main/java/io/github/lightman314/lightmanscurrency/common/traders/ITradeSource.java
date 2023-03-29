package io.github.lightman314.lightmanscurrency.common.traders;

import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;

/**
 * No longer needed as the getTrade & getTradeCount functions are both
 * already defined in the TraderData abstract functions
 */
@Deprecated(since = "2.1.1.0")
public interface ITradeSource<T extends TradeData> {
	T getTrade(int tradeIndex);
	int getTradeCount();
}
