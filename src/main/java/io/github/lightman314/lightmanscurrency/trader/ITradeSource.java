package io.github.lightman314.lightmanscurrency.trader;

import java.util.List;

import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData;

public interface ITradeSource<T extends TradeData> {

	public T getTrade(int index);
	public List<T> getAllTrades();

}