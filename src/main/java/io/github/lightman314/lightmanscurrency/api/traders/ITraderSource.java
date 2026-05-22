package io.github.lightman314.lightmanscurrency.api.traders;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public interface ITraderSource {

	ITraderSource CLIENT_TRADER_SOURCE = new NetworkTraderSource(true);
	ITraderSource SERVER_TRADER_SOURCE = new NetworkTraderSource(false);

    static ITraderSource wrapSource(Supplier<ITraderSource> source) { return new DeferredSource(source); }
    static ITraderSource forTrader(long traderID, IClientTracker context) { return wrapSource(() -> TraderAPI.getApi().GetTrader(context,traderID)); }
    static ITraderSource forBlockEntity(Level level, BlockPos pos) { return wrapSource(() -> {
        if(level.getBlockEntity(pos) instanceof ITraderSource source)
            return source;
        return null;
    }); }
    static ITraderSource forAllNetworkTraders(boolean isClient) { return isClient ? CLIENT_TRADER_SOURCE : SERVER_TRADER_SOURCE; }


    default boolean isValid() {
        List<TraderData> traders = this.getTraders();
        return traders != null && !traders.isEmpty();
    }
	List<TraderData> getTraders();
	boolean isSingleTrader();
	default TraderData getSingleTrader() { return this.getTraders().getFirst(); }

	default boolean showSearchBox() { return false; }
	@Nullable
	default Component getCustomTitle() { return null; }
	
	class NetworkTraderSource implements ITraderSource
	{
		
		private final boolean isClient;
		private NetworkTraderSource(boolean isClient) { this.isClient = isClient; }

		@Nullable
		@Override
		public Component getCustomTitle() { return LCText.GUI_TRADER_ALL_NETWORK_TRADERS.get(); }
		@Override
		public boolean showSearchBox() { return true; }
		
		@Override
		public List<TraderData> getTraders() { return TraderAPI.getApi().GetAllNetworkTraders(this.isClient); }
		@Override
		public boolean isSingleTrader() { return false; }

	}

    class DeferredSource implements ITraderSource
    {
        private final Supplier<ITraderSource> source;
        private DeferredSource(Supplier<ITraderSource> source) { this.source = source; }

        @Override
        public boolean isValid() {
            ITraderSource source = this.source.get();
            return source != null && source.isValid();
        }

        @Override
        public List<TraderData> getTraders() {
            ITraderSource source = this.source.get();
            return source == null ? new ArrayList<>() : source.getTraders();
        }

        @Override
        public boolean isSingleTrader() {
            ITraderSource source = this.source.get();
            return source != null && source.isSingleTrader();
        }

        @Override
        public TraderData getSingleTrader() {
            ITraderSource source = this.source.get();
            return source == null ? null : source.getSingleTrader();
        }

        @Override
        public boolean showSearchBox() {
            ITraderSource source = this.source.get();
            return source != null && source.showSearchBox();
        }

        @Nullable
        @Override
        public Component getCustomTitle() {
            ITraderSource source = this.source.get();
            return source == null ? null : source.getCustomTitle();
        }
    }
	
}
