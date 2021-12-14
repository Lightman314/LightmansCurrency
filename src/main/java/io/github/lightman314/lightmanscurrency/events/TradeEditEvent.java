package io.github.lightman314.lightmanscurrency.events;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraftforge.eventbus.api.Event;

public class TradeEditEvent extends Event{

	private final Supplier<ITrader> traderSource;
	public final ITrader getTrader() { return this.traderSource.get(); }
	protected final int tradeIndex;
	public final int getTradeIndex() { return this.tradeIndex; }
	
	public TradeEditEvent(Supplier<ITrader> trader, int tradeIndex)
	{
		this.traderSource = trader;
		this.tradeIndex = tradeIndex;
	}
	
	public static class TradePriceEditEvent extends TradeEditEvent
	{
		
		private final CoinValue oldPrice;
		public final CoinValue getOldPrice() { return this.oldPrice; }
		public final boolean getWasFree() { return this.oldPrice.isFree(); }
		
		public TradePriceEditEvent(Supplier<ITrader> trader, int tradeIndex, CoinValue oldPrice)
		{
			super(trader, tradeIndex);
			this.oldPrice = oldPrice;
		}
	}
	
}
