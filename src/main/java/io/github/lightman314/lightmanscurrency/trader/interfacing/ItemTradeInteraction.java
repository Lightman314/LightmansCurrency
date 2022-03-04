package io.github.lightman314.lightmanscurrency.trader.interfacing;

import io.github.lightman314.lightmanscurrency.blockentity.UniversalItemTraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class ItemTradeInteraction extends TradeInteraction<ItemTradeInteraction,ItemTradeData>{

	protected final UniversalItemTraderInterfaceBlockEntity blockEntity;
	
	public ItemTradeInteraction(UniversalItemTraderInterfaceBlockEntity parent) {
		super(parent);
		this.blockEntity = parent;
	}
	
	private int claimSlots = 0;
	public int getClaimedSlots() { return this.claimSlots; }
	public void setClaimedSlots(int newAmount) {
		int maxClaimable = this.blockEntity.getTotalClaimedSlots() - this.claimSlots;
		this.claimSlots = MathUtil.clamp(newAmount, 0, maxClaimable);
		this.parent.markTradeInteractionsDirty();
	}

	@Override
	public boolean isValid() {
		if(this.currentMode().trade)
			return this.tradeReference.hasTrade();
		if(this.currentMode().requiresPermissions)
			return this.acceptableTrader();
		return false;
	}

	@Override
	protected void saveAdditionalData(CompoundTag compound) {
		compound.putInt("ClaimedSlots", this.claimSlots);
	}

	@Override
	protected void loadAdditionalData(CompoundTag compound) {
		if(compound.contains("ClaimedSlots", Tag.TAG_INT))
			this.claimSlots = compound.getInt("ClaimedSlots");
	}
	
	@Override
	public boolean validTrader(UniversalTraderData trader) {
		return trader instanceof IItemTrader;
	}
	
	private int getFirstAllowedSlot() {
		if(this.claimSlots <= 0)
			return this.blockEntity.getTotalClaimedSlots();
		else
		{
			int claimedBefore = 0;
			for(ItemTradeInteraction i : this.parent.getInteractions())
			{
				if(i == this)
					break;
				claimedBefore += i.claimSlots;
			}
			return claimedBefore;
		}
	}
	
	private int getStopSlot() {
		if(this.claimSlots <= 0)
			return this.blockEntity.getItemBuffer().getContainerSize();
		else
			return this.getFirstAllowedSlot() + this.claimSlots;
	}
	
	private boolean isSlotAllowed(int slot) {
		return slot >= this.getFirstAllowedSlot() && slot < this.getStopSlot();
	}
	
	public boolean allowItemInput(int slot, ItemStack item) {
		if(!this.isSlotAllowed(slot))
			return false;
		if(this.currentMode().restock && this.acceptableTrader())
		{
			//If this is restock mode, allow any item inputs for items being sold or bartered away
			UniversalTraderData trader = this.tradeReference.getTrader();
			if(trader instanceof IItemTrader)
			{
				IItemTrader it = (IItemTrader)trader;
				for(int i = 0; i < it.getTradeCount(); ++i)
				{
					ItemTradeData trade = it.getTrade(i);
					if(trade.isValid() && (trade.isBarter() || trade.isSale()) && InventoryUtil.ItemMatches(trade.getSellItem(), item))
						return true;
				}
			}
		}
		else if(this.currentMode().trade && this.tradeReference.hasTrade())
		{
			ItemTradeData trade = this.tradeReference.getLocalTrade();
			if(trade != null && trade.isValid())
			{
				ItemStack compareItem = ItemStack.EMPTY;
				if(trade.isPurchase())
					compareItem = trade.getSellItem();
				else if(trade.isBarter())
					compareItem = trade.getBarterItem();
				return !compareItem.isEmpty() && InventoryUtil.ItemMatches(compareItem, item);
			}
		}
		return false;
	}
	
	public boolean allowItemOutput(int slot, ItemStack item) {
		if(!this.isSlotAllowed(slot))
			return false;
		if(this.currentMode().drain)
		{
			UniversalTraderData trader = this.tradeReference.getTrader();
			if(trader instanceof IItemTrader)
			{
				IItemTrader it = (IItemTrader)trader;
				for(int i = 0; i < it.getTradeCount(); ++i)
				{
					ItemTradeData trade = it.getTrade(i);
					if(trade.isValid())
					{
						ItemStack compareItem = ItemStack.EMPTY;
						if(trade.isPurchase())
							compareItem = trade.getSellItem();
						else if(trade.isBarter())
							compareItem = trade.getBarterItem();
						if(!compareItem.isEmpty() && InventoryUtil.ItemMatches(compareItem, item))
							return true;
					}
				}
			}
		}
		else if(this.currentMode().trade)
		{
			ItemTradeData trade = this.tradeReference.getLocalTrade();
			if(trade != null && trade.isValid())
			{
				
			}
		}
		return false;
	}

	@Override
	protected void restockTick() {
		if(this.acceptableTrader())
		{
			UniversalTraderData trader = this.tradeReference.getTrader();
			if(trader instanceof IItemTrader)
			{
				IItemTrader it = (IItemTrader)trader;
				for(int i = 0; i < it.getTradeCount(); ++i)
				{
					ItemTradeData trade = it.getTrade(i);
					if(trade.isValid() && (trade.isSale() || trade.isBarter()))
					{
						ItemStack stockableItem = trade.getSellItem();
						if(!stockableItem.isEmpty() && this.stockItem(it, stockableItem))
							return;
					}
				}
			}
		}
	}
	
	/**
	 * Attempts to move the item from the local storage into the  item trader's storage.
	 * @return Whether the restock was successful, so that the interaction can stop attempting to restock during this tick.
	 */
	private final boolean stockItem(IItemTrader trader, ItemStack item)
	{
		int storedAmount = InventoryUtil.GetItemCount(this.blockEntity.getItemBuffer(), item, this.getFirstAllowedSlot(), this.getStopSlot());
		if(storedAmount > 0)
		{
			//Only move 1 item per tick.
			ItemStack putStack = item.copy();
			putStack.setCount(1);
			if(InventoryUtil.CanPutItemStack(trader.getStorage(), putStack))
			{
				if(InventoryUtil.RemoveItemCount(this.blockEntity.getItemBuffer(), putStack, this.getFirstAllowedSlot(), this.getStopSlot()))
				{
					if(InventoryUtil.PutItemStack(trader.getStorage(), putStack))
						return true;
					else //Put the item back in the item buffer if putting the item in storage failed for some odd reason.
						InventoryUtil.PutItemInSlot(this.blockEntity.getItemBuffer(), putStack, this.getFirstAllowedSlot(), this.getStopSlot());
				}
			}
		}
		return false;
	}

	@Override
	protected void drainTick() {
		if(this.acceptableTrader())
		{
			UniversalTraderData trader = this.tradeReference.getTrader();
			if(trader instanceof IItemTrader)
			{
				IItemTrader it = (IItemTrader)trader;
				for(int i = 0; i < it.getTradeCount(); ++i)
				{
					ItemTradeData trade = it.getTrade(i);
					if(trade.isValid())
					{
						ItemStack drainableItem = ItemStack.EMPTY;
						if(trade.isPurchase())
							drainableItem = trade.getSellItem();
						else if(trade.isBarter())
							drainableItem = trade.getBarterItem();
						if(!drainableItem.isEmpty() && this.drainItem(it, drainableItem))
							return;	
					}
				}
			}
		}
	}
	
	/**
	 * Attempts to move the item from the item trader's storage into the local storage.
	 * @return Whether the drain was successful, so that the interaction can stop attempting to drain during this tick.
	 */
	protected final boolean drainItem(IItemTrader trader, ItemStack item)
	{
		int storedAmount = InventoryUtil.GetItemCount(trader.getStorage(), item);
		if(storedAmount > 0)
		{
			int fittableAmount = InventoryUtil.GetItemSpace(this.blockEntity.getItemBuffer(), item, this.getFirstAllowedSlot(), this.getStopSlot());
			if(fittableAmount > 0)
			{
				//Only move 1 item per tick.
				ItemStack putStack = item.copy();
				putStack.setCount(1);
				if(InventoryUtil.RemoveItemCount(trader.getStorage(), putStack))
				{
					ItemStack leftovers = InventoryUtil.PutItemInSlot(this.blockEntity.getItemBuffer(), putStack, this.getFirstAllowedSlot(), this.getStopSlot());
					if(!leftovers.isEmpty()) //Put the item back in storage if putting the item in the item buffer failed for some odd reason.
						InventoryUtil.PutItemStack(trader.getStorage(), putStack);
					else
						return true;
				}
			}
		}
		return false;
	}

	@Override
	protected void tradeTick() {
		
		if(this.acceptableTrade())
		{
			ItemTradeData trade = this.tradeReference.getTrueTrade();
			boolean canExecute = false;
			if(trade.isPurchase())
			{
				canExecute = InventoryUtil.GetItemCount(this.blockEntity.getItemBuffer(), trade.getSellItem(), this.getFirstAllowedSlot(), this.getStopSlot()) >= trade.getSellItem().getCount();
			}
			else if(trade.isSale())
			{
				canExecute = InventoryUtil.GetItemSpace(this.blockEntity.getItemBuffer(), trade.getSellItem(), this.getFirstAllowedSlot(), this.getStopSlot()) >= trade.getSellItem().getCount();
			}
			else if(trade.isBarter())
			{
				canExecute = InventoryUtil.GetItemSpace(this.blockEntity.getItemBuffer(), trade.getSellItem(), this.getFirstAllowedSlot(), this.getStopSlot()) >= trade.getSellItem().getCount() &&
						InventoryUtil.GetItemCount(this.blockEntity.getItemBuffer(), trade.getBarterItem(), this.getFirstAllowedSlot(), this.getStopSlot()) >= trade.getBarterItem().getCount();
			}
			if(canExecute)
				this.interact();
		}
		
	}

}
