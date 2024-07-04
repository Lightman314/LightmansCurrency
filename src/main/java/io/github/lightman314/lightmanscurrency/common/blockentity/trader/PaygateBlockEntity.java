package io.github.lightman314.lightmanscurrency.common.blockentity.trader;

import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.PaygateBlock;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PaygateBlockEntity extends TraderBlockEntity<PaygateTraderData> {
	
	private int timer = 0;
	
	public PaygateBlockEntity(BlockPos pos, BlockState state) { this(ModBlockEntities.PAYGATE.get(), pos, state); }
	
	protected PaygateBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

	@Nullable
	@Override
	protected PaygateTraderData castOrNullify(@Nonnull TraderData trader) {
		if(trader instanceof PaygateTraderData pg)
			return pg;
		return null;
	}

	@Override
	public void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		
		super.saveAdditional(compound,lookup);
		
		this.saveTimer(compound);
		
	}
	
	public final CompoundTag saveTimer(CompoundTag compound) {
		compound.putInt("Timer", Math.max(this.timer, 0));
		return compound;
	}
	
	@Override
	public void loadAdditional(@Nonnull CompoundTag compound,@Nonnull HolderLookup.Provider lookup) {
		
		//Load the timer
		if(compound.contains("Timer", Tag.TAG_INT))
			this.timer = Math.max(compound.getInt("Timer"), 0);
		
		super.loadAdditional(compound,lookup);
		
	}
	
	public boolean isActive() { return this.timer > 0; }
	
	public void activate(int duration) {
		this.timer = duration;
		this.level.setBlockAndUpdate(this.worldPosition, this.level.getBlockState(this.worldPosition).setValue(PaygateBlock.POWERED, true));
		this.markTimerDirty();
	}
	
	@Override
	public void serverTick()
	{
		super.serverTick();
		if(this.timer > 0)
		{
			this.timer--;
			this.markTimerDirty();
			if(this.timer <= 0)
			{
				this.level.setBlockAndUpdate(this.worldPosition, this.level.getBlockState(this.worldPosition).setValue(PaygateBlock.POWERED, false));
			}
		}
	}
	
	public void markTimerDirty() {
		this.setChanged();
		if(!this.level.isClientSide)
			BlockEntityUtil.sendUpdatePacket(this, this.saveTimer(new CompoundTag()));
	}
	
	public int getValidTicketTrade(Player player, ItemStack heldItem) {
		PaygateTraderData trader = this.getTraderData();
		if(TicketItem.isTicketOrPass(heldItem))
		{
			long ticketID = TicketItem.GetTicketID(heldItem);
			if(ticketID >= -1)
			{
				TradeContext context = TradeContext.create(trader,player).build();
				for(int i = 0; i < trader.getTradeCount(); ++i)
				{
					PaygateTradeData trade = trader.getTrade(i);
					if(trade.isTicketTrade() && trade.getTicketID() == ticketID)
					{
						//Confirm that the player is allowed to access the trade
						if(!trader.runPreTradeEvent(trade, context).isCanceled())
							return i;
					}
				}
			}
		}
		return -1;
	}

	@Nonnull
    @Override
	protected PaygateTraderData buildNewTrader() { return new PaygateTraderData(this.level, this.worldPosition); }
	
}
