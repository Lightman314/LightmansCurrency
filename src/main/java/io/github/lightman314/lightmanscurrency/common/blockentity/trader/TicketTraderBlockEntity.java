package io.github.lightman314.lightmanscurrency.common.blockentity.trader;

import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderDataTicket;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import net.minecraft.nbt.CompoundNBT;


public class TicketTraderBlockEntity extends ItemTraderBlockEntity{
	
	
	public TicketTraderBlockEntity()
	{
		super(ModBlockEntities.TICKET_TRADER.get());
	}
	
	public TicketTraderBlockEntity(int tradeCount)
	{
		super(ModBlockEntities.TICKET_TRADER.get(), tradeCount);
	}
	
	@Override
	public ItemTraderData buildNewTrader() { return new ItemTraderDataTicket(this.tradeCount, this.level, this.worldPosition); }
	
	@Override @Deprecated
	protected ItemTraderData createTraderFromOldData(CompoundNBT compound) {
		ItemTraderDataTicket newTrader = new ItemTraderDataTicket(1, this.level, this.worldPosition);
		newTrader.loadOldUniversalTraderData(compound);
		this.tradeCount = newTrader.getTradeCount();
		return newTrader;
	}
	
}