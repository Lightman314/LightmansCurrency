package io.github.lightman314.lightmanscurrency.common.traders;

import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;

public interface ITradeSource<T extends TradeData> {
	public T getTrade(int tradeIndex);
	public int getTradeCount();
}
