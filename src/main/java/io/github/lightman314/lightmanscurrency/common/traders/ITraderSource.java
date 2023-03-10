package io.github.lightman314.lightmanscurrency.common.traders;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

public interface ITraderSource {

	ITraderSource CLIENT_TRADER_SOURCE = new NetworkTraderSource(true);
	ITraderSource SERVER_TRADER_SOURCE = new NetworkTraderSource(false);
	
	@Nonnull
	List<TraderData> getTraders();
	boolean isSingleTrader();
	default TraderData getSingleTrader() { return this.getTraders().get(0); }
	
	static Supplier<ITraderSource> UniversalTraderSource(boolean isClient) { return isClient ? () -> CLIENT_TRADER_SOURCE : () -> SERVER_TRADER_SOURCE; }
	
	class NetworkTraderSource implements ITraderSource
	{
		
		private final boolean isClient;
		public NetworkTraderSource(boolean isClient) { this.isClient = isClient; }
		
		@Nonnull
		@Override
		public List<TraderData> getTraders() { return TraderSaveData.GetAllTerminalTraders(this.isClient); }
		@Override
		public boolean isSingleTrader() { return false; }
		
	}
	
}