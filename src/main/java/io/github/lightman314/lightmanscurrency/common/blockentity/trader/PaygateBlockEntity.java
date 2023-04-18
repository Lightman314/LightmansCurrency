package io.github.lightman314.lightmanscurrency.common.blockentity.trader;

import io.github.lightman314.lightmanscurrency.common.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.PaygateBlock;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PaygateBlockEntity extends TraderBlockEntity<PaygateTraderData> {
	
	private int timer = 0;
	
	public PaygateBlockEntity() { this(ModBlockEntities.PAYGATE.get()); }
	
	protected PaygateBlockEntity(TileEntityType<?> type) { super(type); }
	

    @Override
	protected void saveAdditional(@Nonnull CompoundNBT compound) {
		
		super.saveAdditional(compound);
		
		this.saveTimer(compound);
		
	}
	
	public final CompoundNBT saveTimer(CompoundNBT compound) {
		compound.putInt("Timer", Math.max(this.timer, 0));
		return compound;
	}
	
	@Override
	protected void loadAdditional(@Nonnull CompoundNBT compound) {

		super.loadAdditional(compound);

		//Load the timer
		if(compound.contains("Timer", Constants.NBT.TAG_INT))
			this.timer = Math.max(compound.getInt("Timer"), 0);
		
	}
	
	public boolean isActive() { return this.timer > 0; }
	
	public void activate(int duration) {
		this.timer = duration;
		this.level.setBlockAndUpdate(this.worldPosition, this.level.getBlockState(this.worldPosition).setValue(PaygateBlock.POWERED, true));
		this.markTimerDirty();
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(this.isClient())
			return;
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
			BlockEntityUtil.sendUpdatePacket(this, this.saveTimer(new CompoundNBT()));
	}
	
	public int getValidTicketTrade(PlayerEntity player, ItemStack heldItem) {
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
	protected PaygateTraderData createTraderFromOldData(CompoundNBT compound) {
		PaygateTraderData newTrader = new PaygateTraderData(this.level, this.worldPosition);
		newTrader.loadOldBlockEntityData(compound);
		if(compound.contains("Timer"))
			this.timer = compound.getInt("Timer");
		return newTrader;
	}

	@Override
	protected void loadAsFormerNetworkTrader(@Nullable PaygateTraderData trader, CompoundNBT compound) { }

}