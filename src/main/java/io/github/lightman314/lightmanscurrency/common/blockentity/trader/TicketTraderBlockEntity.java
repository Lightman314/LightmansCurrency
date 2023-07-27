package io.github.lightman314.lightmanscurrency.common.blockentity.trader;

import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderDataTicket;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;


public class TicketTraderBlockEntity extends ItemTraderBlockEntity{
	
	
	public TicketTraderBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModBlockEntities.TICKET_TRADER.get(), pos, state);
	}
	
	public TicketTraderBlockEntity(BlockPos pos, BlockState state, int tradeCount)
	{
		super(ModBlockEntities.TICKET_TRADER.get(), pos, state, tradeCount);
	}
	
	@Nonnull
    @Override
	public ItemTraderData buildNewTrader() { return new ItemTraderDataTicket(this.tradeCount, this.level, this.worldPosition); }
	
}
