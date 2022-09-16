package io.github.lightman314.lightmanscurrency.common.traders;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

public interface ITraderSource {

	public static final ITraderSource CLIENT_TRADER_SOURCE = new NetworkTraderSource(true);
	public static final ITraderSource SERVER_TRADER_SOURCE = new NetworkTraderSource(false);
	
	@Nonnull
	public List<TraderData> getTraders();
	public boolean isSingleTrader();
	public default TraderData getSingleTrader() { return this.getTraders().get(0); }
	
	public static Supplier<ITraderSource> UniversalTraderSource(boolean isClient) { return isClient ? () -> CLIENT_TRADER_SOURCE : () -> SERVER_TRADER_SOURCE; }
	
	public static class NetworkTraderSource implements ITraderSource
	{
		
		private final boolean isClient;
		public NetworkTraderSource(boolean isClient) { this.isClient = isClient; }
		
		@Override
		public List<TraderData> getTraders() { return TraderSaveData.GetAllTerminalTraders(this.isClient); }
		@Override
		public boolean isSingleTrader() { return false; }
		
	}
	
}
