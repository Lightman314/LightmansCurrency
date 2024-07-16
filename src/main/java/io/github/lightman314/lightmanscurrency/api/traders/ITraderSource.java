package io.github.lightman314.lightmanscurrency.api.traders;

import io.github.lightman314.lightmanscurrency.LCText;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ITraderSource {

	ITraderSource CLIENT_TRADER_SOURCE = new NetworkTraderSource(true);
	ITraderSource SERVER_TRADER_SOURCE = new NetworkTraderSource(false);

	@Nonnull
	List<TraderData> getTraders();
	boolean isSingleTrader();
	@Nonnull
	default TraderData getSingleTrader() { return this.getTraders().get(0); }


	default boolean showSearchBox() { return false; }
	@Nullable
	default Component getCustomTitle() { return null; }

	static Supplier<ITraderSource> NetworkTraderSource(boolean isClient) { return isClient ? () -> CLIENT_TRADER_SOURCE : () -> SERVER_TRADER_SOURCE; }

	class NetworkTraderSource implements ITraderSource
	{

		private final boolean isClient;
		private NetworkTraderSource(boolean isClient) { this.isClient = isClient; }

		@Nullable
		@Override
		public Component getCustomTitle() { return LCText.GUI_TRADER_ALL_NETWORK_TRADERS.get(); }
		@Override
		public boolean showSearchBox() { return true; }
		@Nonnull
		@Override
		public List<TraderData> getTraders() { return TraderAPI.API.GetAllNetworkTraders(this.isClient); }
		@Override
		public boolean isSingleTrader() { return false; }

	}

}