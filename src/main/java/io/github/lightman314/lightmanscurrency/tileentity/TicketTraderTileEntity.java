package io.github.lightman314.lightmanscurrency.tileentity;

import io.github.lightman314.lightmanscurrency.core.ModTileEntities;
import io.github.lightman314.lightmanscurrency.trader.tradedata.restrictions.ItemTradeRestriction;


public class TicketTraderTileEntity extends ItemTraderTileEntity{
	
	public TicketTraderTileEntity()
	{
		super(ModTileEntities.TICKET_TRADER);
		this.validateTradeLimitations();
	}
	
	public TicketTraderTileEntity(int tradeCount)
	{
		super(ModTileEntities.TICKET_TRADER, tradeCount);
		this.validateTradeLimitations();
	}
	
	private void validateTradeLimitations()
	{
		for(int i = 0; i < this.tradeCount; i++)
		{
			this.restrictTrade(i, ItemTradeRestriction.TICKET_KIOSK);
		}
	}
	
	@Override
	public void tick()
	{
		
		super.tick();
		
		this.validateTradeLimitations();
		
	}
	
}
