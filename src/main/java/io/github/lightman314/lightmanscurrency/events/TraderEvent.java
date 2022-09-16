package io.github.lightman314.lightmanscurrency.events;

import io.github.lightman314.lightmanscurrency.common.ownership.OwnerData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

public abstract class TraderEvent extends Event{
	
	private final long traderID;
	public final long getID() { return this.traderID; }
	public final OwnerData getOwner() { return this.getTrader() == null ? null : this.getTrader().getOwner(); }
	public TraderData getTrader() { return TraderSaveData.GetTrader(false, this.traderID); }
	
	protected TraderEvent(long traderID)
	{
		this.traderID = traderID;
	}
	
	public static class UniversalTradeCreateEvent extends TraderEvent
	{
		
		private final Player player;
		public Player getPlayer() { return this.player; }
		
		public UniversalTradeCreateEvent(long traderID, Player player)
		{
			super(traderID);
			this.player = player;
		}
	}
	
	public static class UniversalTradeRemoveEvent extends TraderEvent
	{
		
		private final TraderData data;
		@Override
		public TraderData getTrader() { return this.data; }
		
		public UniversalTradeRemoveEvent(long traderID, TraderData removedData)
		{
			super(traderID);
			this.data = removedData;
		}
	}
	
}
