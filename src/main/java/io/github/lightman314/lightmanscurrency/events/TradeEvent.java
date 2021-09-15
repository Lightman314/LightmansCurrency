package io.github.lightman314.lightmanscurrency.events;

import io.github.lightman314.lightmanscurrency.tradedata.TradeData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraftforge.eventbus.api.Event;

public abstract class TradeEvent extends Event{

	private final PlayerEntity player;
	public final PlayerEntity getPlayer() { return this.player; }
	private final TradeData trade;
	public final TradeData getTrade() { return this.trade; }
	private final Container container;
	public final Container getContainer() { return this.container; }
	
	protected TradeEvent(PlayerEntity player, TradeData trade, Container container)
	{
		this.player = player;
		this.trade = trade;
		this.container = container;
	}
	
	public static class PreTradeEvent extends TradeEvent
	{
		public PreTradeEvent(PlayerEntity player, TradeData trade, Container container)
		{
			super(player, trade, container);
		}
		
		@Override
		public boolean isCancelable() { return true; }
		
	}
	
	public static class PostTradeEvent extends TradeEvent
	{
		public PostTradeEvent(PlayerEntity player, TradeData trade, Container container)
		{
			super(player, trade, container);
		}
	}
	
}
