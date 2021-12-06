package io.github.lightman314.lightmanscurrency.blockentity;

import io.github.lightman314.lightmanscurrency.core.ModTileEntities;
import io.github.lightman314.lightmanscurrency.trader.tradedata.restrictions.ItemTradeRestriction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;


public class TicketTraderBlockEntity extends ItemTraderBlockEntity{
	
	public TicketTraderBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModTileEntities.TICKET_TRADER, pos, state);
		this.validateTradeLimitations();
	}
	
	public TicketTraderBlockEntity(BlockPos pos, BlockState state, int tradeCount)
	{
		super(ModTileEntities.TICKET_TRADER, pos, state, tradeCount);
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
