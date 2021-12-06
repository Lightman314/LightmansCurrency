package io.github.lightman314.lightmanscurrency.events;

import java.util.UUID;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

public abstract class UniversalTraderEvent extends Event{
	
	private final UUID traderID;
	public final UUID getID() { return this.traderID; }
	public final UUID getOwnerID() { return this.getData() == null ? null : this.getData().getOwnerID(); }
	public UniversalTraderData getData() { return TradingOffice.getData(this.traderID); }
	
	protected UniversalTraderEvent(UUID traderID)
	{
		this.traderID = traderID;
	}
	
	public static class UniversalTradeCreateEvent extends UniversalTraderEvent
	{
		
		private final Player owner;
		public Player getOwner() { return this.owner; }
		
		public UniversalTradeCreateEvent(UUID traderID, Player owner)
		{
			super(traderID);
			this.owner = owner;
		}
	}
	
	public static class UniversalTradeRemoveEvent extends UniversalTraderEvent
	{
		
		private final UniversalTraderData data;
		@Override
		public UniversalTraderData getData() { return this.data; }
		
		public UniversalTradeRemoveEvent(UUID traderID, UniversalTraderData removedData)
		{
			super(traderID);
			this.data = removedData;
		}
	}
	
}
