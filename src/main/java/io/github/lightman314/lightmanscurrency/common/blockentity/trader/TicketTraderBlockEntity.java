package io.github.lightman314.lightmanscurrency.common.blockentity.trader;

import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderDataTicket;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;


public class TicketTraderBlockEntity extends ItemTraderBlockEntity{
	
	
	public TicketTraderBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModBlockEntities.TICKET_TRADER.get(), pos, state);
	}
	
	public TicketTraderBlockEntity(BlockPos pos, BlockState state, int tradeCount)
	{
		super(ModBlockEntities.TICKET_TRADER.get(), pos, state, tradeCount);
	}
	
	@Override
	public ItemTraderData buildNewTrader() { return new ItemTraderDataTicket(this.tradeCount, this.level, this.worldPosition); }
	
	@Override @Deprecated
	protected ItemTraderData createTraderFromOldData(CompoundTag compound) {
		ItemTraderDataTicket newTrader = new ItemTraderDataTicket(1, this.level, this.worldPosition);
		newTrader.loadOldUniversalTraderData(compound);
		this.tradeCount = newTrader.getTradeCount();
		return newTrader;
	}
	
}