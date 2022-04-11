package io.github.lightman314.lightmanscurrency.blockentity;

import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.trader.tradedata.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.trader.tradedata.restrictions.TicketKioskRestriction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;


public class TicketTraderBlockEntity extends ItemTraderBlockEntity{
	
	public TicketTraderBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModBlockEntities.TICKET_TRADER, pos, state);
	}
	
	public TicketTraderBlockEntity(BlockPos pos, BlockState state, int tradeCount)
	{
		super(ModBlockEntities.TICKET_TRADER, pos, state, tradeCount);
	}
	
	@Override
	public ItemTradeRestriction getRestriction(int tradeIndex) { return TicketKioskRestriction.INSTANCE; }
	
}
