package io.github.lightman314.lightmanscurrency.common.blockentity;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.handler.ItemInterfaceHandler;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.common.traders.item.TraderItemStorage.ITraderItemFilter;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.item.ItemStorageTab;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.CapacityUpgrade;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class ItemTraderInterfaceBlockEntity extends TraderInterfaceBlockEntity implements ITraderItemFilter{
	
	private final TraderItemStorage itemBuffer = new TraderItemStorage(this);
	public TraderItemStorage getItemBuffer() { return this.itemBuffer; }
	
	ItemInterfaceHandler itemHandler;
	public ItemInterfaceHandler getItemHandler() { return this.itemHandler; }
	
	public ItemTraderInterfaceBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.TRADER_INTERFACE_ITEM.get(), pos, state);
		this.itemHandler = this.addHandler(new ItemInterfaceHandler(this, this::getItemBuffer));
	}

	@Override
	public TradeContext.Builder buildTradeContext(TradeContext.Builder baseContext) {
		return baseContext.withItemHandler(this.itemBuffer);
	}
	
	public boolean allowInput(ItemStack item) {
		if(this.getInteractionType().trades)
		{
			//Check trade for barter items to restock
			TradeData t = this.getReferencedTrade();
			if(t instanceof ItemTradeData trade)
			{
				if(trade.isBarter())
				{
					for(int i = 0; i < 2; ++i)
					{
						if(InventoryUtil.ItemMatches(item, trade.getBarterItem(i)))
							return true;
					}
				}
				else if(trade.isPurchase())
				{
					for(int i = 0; i < 2; ++i)
					{
						if(InventoryUtil.ItemMatches(item, trade.getSellItem(i)))
							return true;
					}
				}
			}
		}
		else
		{
			//Scan all trades for sale items to restock
			TraderData trader = this.getTrader();
			if(trader instanceof ItemTraderData)
			{
				for(ItemTradeData trade : ((ItemTraderData) trader).getTradeData())
				{
					if(trade.isSale() || trade.isBarter())
					{
						for(int i = 0; i < 2; ++i)
						{
							if(InventoryUtil.ItemMatches(item, trade.getSellItem(i)))
								return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public boolean allowOutput(ItemStack item) {
		return !this.allowInput(item);
	}
	
	@Override
	public boolean isItemRelevant(ItemStack item) {
		if(this.getInteractionType().trades)
		{
			TradeData t = this.getReferencedTrade();
			if(t instanceof ItemTradeData trade)
			{
				return trade.allowItemInStorage(item);
			}
		}
		else
		{
			TraderData trader = this.getTrader();
			if(trader instanceof ItemTraderData it)
			{
				for(ItemTradeData trade : it.getTradeData())
				{
					if(trade.allowItemInStorage(item))
						return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public int getStorageStackLimit() { 
		int limit = ItemTraderData.DEFAULT_STACK_LIMIT;
		for(int i = 0; i < this.getUpgradeInventory().getContainerSize(); ++i)
		{
			ItemStack stack = this.getUpgradeInventory().getItem(i);
			if(stack.getItem() instanceof UpgradeItem upgradeItem)
			{
				if(this.allowUpgrade(upgradeItem))
				{
					if(upgradeItem.getUpgradeType() instanceof CapacityUpgrade)
					{
						limit += UpgradeItem.getUpgradeData(stack).getIntValue(CapacityUpgrade.CAPACITY);
					}
				}
			}
		}
		return limit;
	}
	
	@Override
	protected ItemTradeData deserializeTrade(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) { return ItemTradeData.loadData(compound, false, lookup); }
	
	@Override
	protected void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		super.saveAdditional(compound,lookup);
		this.saveItemBuffer(compound,lookup);
	}
	
	protected final CompoundTag saveItemBuffer(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		this.itemBuffer.save(compound, "Storage",lookup);
		return compound;
	}
	
	public void setItemBufferDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveItemBuffer(new CompoundTag(),this.registryAccess()));
	}
	
	@Override
	public void loadAdditional(CompoundTag compound,@Nonnull HolderLookup.Provider lookup) {
		super.loadAdditional(compound,lookup);
		if(compound.contains("Storage"))
			this.itemBuffer.load(compound, "Storage",lookup);
	}

	@Override
	public boolean validTraderType(TraderData trader) { return trader instanceof ItemTraderData; }
	
	protected final ItemTraderData getItemTrader() {
		TraderData trader = this.getTrader();
		if(trader instanceof ItemTraderData)
			return (ItemTraderData)trader;
		return null;
	}
	
	@Override
	protected void drainTick() {
		ItemTraderData trader = this.getItemTrader();
		if(trader != null && trader.hasPermission(this.owner.getPlayerForContext(), Permissions.INTERACTION_LINK))
		{
			for(int i = 0; i < trader.getTradeCount(); ++i)
			{
				ItemTradeData trade = trader.getTrade(i);
				if(trade.isValid())
				{
					List<ItemStack> drainItems = new ArrayList<>();
					if(trade.isPurchase())
					{
						drainItems.add(trade.getSellItem(0));
						drainItems.add(trade.getSellItem(1));
					}
					
					if(trade.isBarter())
					{
						drainItems.add(trade.getBarterItem(0));
						drainItems.add(trade.getBarterItem(1));
					}
					for(ItemStack drainItem : drainItems)
					{
						if(!drainItem.isEmpty())
						{
							//Drain the item from the trader
							int drainableAmount = trader.getStorage().getItemCount(drainItem);
							if(drainableAmount > 0)
							{
								ItemStack movingStack = drainItem.copy();
								movingStack.setCount(Math.min(movingStack.getMaxStackSize(), drainableAmount));
								//Remove the stack from storage
								ItemStack removed = trader.getStorage().removeItem(movingStack);
								//InventoryUtil.RemoveItemCount(trader.getStorage(), movingStack);
								//Put the stack in the item buffer (if possible)
								ItemStack leftovers = ItemHandlerHelper.insertItemStacked(this.itemBuffer, removed, false);
								//If some items couldn't be put in the item buffer, put them back in storage
								if(!leftovers.isEmpty())
									trader.getStorage().forceAddItem(leftovers);
								this.setItemBufferDirty();
								trader.markStorageDirty();
							}
						}
					}
				}
			}
		}
	}

	@Override
	protected void restockTick() {
		ItemTraderData trader = this.getItemTrader();
		if(trader != null && trader.hasPermission(this.owner.getPlayerForContext(), Permissions.INTERACTION_LINK))
		{
			for(int i = 0; i < trader.getTradeCount(); ++i)
			{
				ItemTradeData trade = trader.getTrade(i);
				if(trade.isValid() && (trade.isBarter() || trade.isSale()))
				{
					for(int s = 0; s < 2; ++s)
					{
						ItemStack stockItem = trade.getSellItem(s);
						if(!stockItem.isEmpty())
						{
							int stockableAmount = this.itemBuffer.getItemCount(stockItem);
							if(stockableAmount > 0)
							{
								ItemStack movingStack = stockItem.copy();
								movingStack.setCount(Math.min(movingStack.getMaxStackSize(), stockableAmount));
								//Remove the item from the item buffer
								ItemStack removedItem = this.itemBuffer.removeItem(movingStack);
								if(removedItem.getCount() == movingStack.getCount())
								{
									trader.getStorage().tryAddItem(movingStack);
									if(!movingStack.isEmpty())
									{
										//Place the leftovers back in storage
										this.itemBuffer.forceAddItem(movingStack);
									}
								}
								else
									this.itemBuffer.forceAddItem(removedItem);
								this.setItemBufferDirty();
								trader.markStorageDirty();
							}
						}
					}
				}
			}
		}
	}

	@Override
	protected void tradeTick() {
		TradeData t = this.getTrueTrade();
		if(t instanceof ItemTradeData trade)
		{
			if(trade != null && trade.isValid())
			{
				if(trade.isSale())
				{
					//Confirm that we have enough space to store the purchased item(s)
					if(this.itemBuffer.canFitItems(trade.getSellItem(0), trade.getSellItem(1)))
					{
						this.interactWithTrader();
						this.setItemBufferDirty();
					}
				}
				else if(trade.isPurchase())
				{
					//Confirm that we have enough of the item in storage to sell the item(s)
					if(this.itemBuffer.hasItems(trade.getSellItem(0), trade.getSellItem(1)))
					{
						this.interactWithTrader();
						this.setItemBufferDirty();
					}
				}
				else if(trade.isBarter())
				{
					//Confirm that we have enough space to store the purchased item AND
					//That we have enough of the item in storage to barter away.
					if(this.itemBuffer.hasItems(trade.getBarterItem(0), trade.getBarterItem(1)) && this.itemBuffer.canFitItems(trade.getSellItem(0), trade.getSellItem(1)))
					{
						this.interactWithTrader();
						this.setItemBufferDirty();
					}
				}
			}
		}
		
	}
	
	@Override
	protected void hopperTick() {
		boolean markBufferDirty = false;
		for(Direction relativeSide : Direction.values())
		{
			if(this.itemHandler.getInputSides().get(relativeSide) || this.itemHandler.getOutputSides().get(relativeSide))
			{
				Direction actualSide = relativeSide;
				if(this.getBlockState().getBlock() instanceof IRotatableBlock b)
					actualSide = IRotatableBlock.getActualSide(b.getFacing(this.getBlockState()), relativeSide);
				
				BlockPos queryPos = this.worldPosition.relative(actualSide);
				IItemHandler itemHandler = this.level.getCapability(Capabilities.ItemHandler.BLOCK, queryPos, actualSide.getOpposite());
				if(itemHandler != null)
				{
					//Collect items from neighboring blocks
					if(this.itemHandler.getInputSides().get(relativeSide))
					{
						boolean query = true;
						for(int i = 0; query && i < itemHandler.getSlots(); ++i)
						{
							ItemStack stack = itemHandler.getStackInSlot(i);
							int fittableAmount = this.itemBuffer.getFittableAmount(stack);
							if(fittableAmount > 0)
							{
								query = false;
								ItemStack result = itemHandler.extractItem(i, fittableAmount, false);
								this.itemBuffer.forceAddItem(result);
								markBufferDirty = true;
							}
						}
					}
					//Attempt to place items to neighboring blocks
					if(this.itemHandler.getOutputSides().get(relativeSide)) {
						List<ItemStack> buffer = this.itemBuffer.getContents();
						boolean query = true;
						for (int i = 0; query && i < buffer.size(); ++i) {
							ItemStack stack = buffer.get(i).copy();
							if (this.allowOutput(stack)) {
								for (int slot = 0; query && slot < itemHandler.getSlots(); ++slot) {
									ItemStack result = itemHandler.insertItem(slot, stack.copy(), false);
									int placed = stack.getCount() - result.getCount();
									if (placed > 0) {
										query = false;
										stack.setCount(placed);
										this.itemBuffer.removeItem(stack);
										markBufferDirty = true;
									}
								}
							}
						}
					}
				}
				
			}
		}
		if(markBufferDirty)
			this.setItemBufferDirty();
		
	}
	
	@Override
	public void initMenuTabs(TraderInterfaceMenu menu) {
		menu.setTab(TraderInterfaceTab.TAB_STORAGE, new ItemStorageTab(menu));
	}
	
	@Override
	public boolean allowAdditionalUpgrade(UpgradeType type) { return type == Upgrades.ITEM_CAPACITY; }

	@Override
	public void getAdditionalContents(List<ItemStack> contents) {
		
		contents.addAll(this.itemBuffer.getSplitContents());
		
	}

	@Override
	public MutableComponent getName() {
		return Component.translatable("block.lightmanscurrency.item_trader_interface");
	}
	
}
