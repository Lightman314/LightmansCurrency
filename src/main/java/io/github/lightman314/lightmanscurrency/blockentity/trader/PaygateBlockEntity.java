package io.github.lightman314.lightmanscurrency.blockentity.trader;

import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.PaygateBlock;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.paygate.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.items.TicketItem;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class PaygateBlockEntity extends TraderBlockEntity<PaygateTraderData> {
	
	private int timer = 0;
	
	public PaygateBlockEntity(BlockPos pos, BlockState state) { this(ModBlockEntities.PAYGATE.get(), pos, state); }
	
	protected PaygateBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }
	
	
	@Override
	public void saveAdditional(@NotNull CompoundTag compound) {
		
		super.saveAdditional(compound);
		
		this.saveTimer(compound);
		
	}
	
	public final CompoundTag saveTimer(CompoundTag compound) {
		compound.putInt("Timer", Math.max(this.timer, 0));
		return compound;
	}
	
	@Override
	public void load(@NotNull CompoundTag compound) {
		
		//Load the timer
		if(compound.contains("Timer", Tag.TAG_INT))
			this.timer = Math.max(compound.getInt("Timer"), 0);
		
		super.load(compound);
		
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
		if(heldItem.getItem() == ModItems.TICKET.get())
		{
			long ticketID = TicketItem.GetTicketID(heldItem);
			if(ticketID >= -1)
			{
				for(int i = 0; i < trader.getTradeCount(); ++i)
				{
					PaygateTradeData trade = trader.getTrade(i);
					if(trade.isTicketTrade() && trade.getTicketID() == ticketID)
					{
						//Confirm that the player is allowed to access the trade
						if(!trader.runPreTradeEvent(PlayerReference.of(player), trade).isCanceled())
							return i;
					}
				}
			}
		}
		return -1;
	}

	@Override
	protected PaygateTraderData buildNewTrader() { return new PaygateTraderData(this.level, this.worldPosition); }

	@Override @Deprecated
	protected PaygateTraderData createTraderFromOldData(CompoundTag compound) {
		PaygateTraderData newTrader = new PaygateTraderData(this.level, this.worldPosition);
		newTrader.loadOldBlockEntityData(compound);
		if(compound.contains("Timer"))
			this.timer = compound.getInt("Timer");
		return newTrader;
	}
	
}