package io.github.lightman314.lightmanscurrency.trader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;

public interface ITraderSource {

	public static final ITraderSource CLIENT_TRADER_SOURCE = new ClientTraderSource();
	public static final ITraderSource SERVER_TRADER_SOURCE = new ServerTraderSource();
	
	@Nonnull
	public List<ITrader> getTraders();
	public boolean isSingleTrader();
	public default ITrader getSingleTrader() { return this.getTraders().get(0); }
	
	public static Supplier<ITraderSource> UniversalTraderSource(boolean isClient) { return isClient ? () -> CLIENT_TRADER_SOURCE : () -> SERVER_TRADER_SOURCE; }
	
	public static class ClientTraderSource implements ITraderSource
	{
		@Override
		public List<ITrader> getTraders() {
			List<ITrader> traders = new ArrayList<>();
			for(UniversalTraderData trader : ClientTradingOffice.getTraderList())
				traders.add(trader);
			return traders;
		}
		@Override
		public boolean isSingleTrader() { return false; }
	}
	
	public static class ServerTraderSource implements ITraderSource
	{
		@Override
		public List<ITrader> getTraders() {
			List<ITrader> traders = new ArrayList<>();
			for(UniversalTraderData trader : TradingOffice.getTraders())
				traders.add(trader);
			return traders;
		}
		@Override
		public boolean isSingleTrader() { return false; }
	}
	
}
