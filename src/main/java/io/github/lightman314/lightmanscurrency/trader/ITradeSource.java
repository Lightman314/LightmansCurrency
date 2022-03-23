package io.github.lightman314.lightmanscurrency.trader;

import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData;

public interface ITradeSource<T extends TradeData> {
	public T getTrade(int tradeIndex);
	public int getTradeCount();
}
