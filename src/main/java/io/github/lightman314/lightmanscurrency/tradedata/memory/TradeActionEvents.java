package io.github.lightman314.lightmanscurrency.tradedata.memory;

import io.github.lightman314.lightmanscurrency.tradedata.TradeData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.eventbus.api.Event;

public class TradeActionEvents {

	public abstract class TradeEvent<T extends TradeData<?>> extends Event
	{
		
		private final PlayerEntity player;
		public final PlayerEntity getPlayer() { return this.player; }
		private final T trade;
		public final T getTrade() { return this.trade; }
		
		protected TradeEvent(PlayerEntity player, T trade)
		{
			this.player = player;
			this.trade = trade;
		}
	}
	
	public class PreTradeEvent<T extends TradeData<?>> extends TradeEvent<T>
	{
		public PreTradeEvent(PlayerEntity player, T trade)
		{
			super(player, trade);
		}
		
		@Override
		public boolean isCancelable() { return true; }
		
	}
	
	public class PostTradeEvent<T extends TradeData<?>> extends TradeEvent<T>
	{
		public PostTradeEvent(PlayerEntity player, T trade)
		{
			super(player, trade);
		}
		
	}
	
}
