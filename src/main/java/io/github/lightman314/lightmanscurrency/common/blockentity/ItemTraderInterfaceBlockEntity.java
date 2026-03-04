package io.github.lightman314.lightmanscurrency.common.blockentity;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.api.trader_interface.data.TradeReference;
import io.github.lightman314.lightmanscurrency.common.blockentity.handler.ItemInterfaceHandler;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.nodes.ItemStorageNode;
import io.github.lightman314.lightmanscurrency.common.traders.item.nodes.ItemTradeNode;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.item.ItemStorageTab;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import io.github.lightman314.lightmanscurrency.api.upgrades.types.CapacityUpgrade;
import io.github.lightman314.lightmanscurrency.util.ItemRequirement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class ItemTraderInterfaceBlockEntity extends TraderInterfaceBlockEntity<ItemTradeData> {
	
	private final TraderItemStorage itemBuffer = new TraderItemStorage()
            .withFilter(this::isItemRelevant)
            .withStorageLimit(this::getStorageStackLimit)
            .withListener(this::setItemBufferDirty);
	public TraderItemStorage getItemBuffer() { return this.itemBuffer; }
	
	ItemInterfaceHandler itemHandler;
	public ItemInterfaceHandler getItemHandler() { return this.itemHandler; }
	
	public ItemTraderInterfaceBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.TRADER_INTERFACE_ITEM.get(), pos, state);
		this.itemHandler = this.addHandler(new ItemInterfaceHandler(this));
	}

    @Override
    public Codec<ItemTradeData> tradeCodec() { return ItemTradeData.CODEC; }

    @Override
	public TradeContext.Builder buildTradeContext(TradeContext.Builder baseContext) {
		return baseContext.withItemHandler(this.itemBuffer);
	}
	
	public boolean allowInput(ItemStack item) {
		if(this.getInteractionType().trades())
		{
			//Check trade for barter items to restock
			for(TradeReference<ItemTradeData> t : this.targets.getTradeReferences())
			{
				TradeData t2 = t.getLocalTrade();
				if(t2 instanceof ItemTradeData trade)
				{
					if(trade.isBarter())
					{
						for(int i = 0; i < 2; ++i)
						{
                            ItemRequirement requirement = trade.getItemRequirement(i + 2);
                            if(requirement.test(item))
                                return true;
						}
					}
					else if(trade.isPurchase())
					{
						for(int i = 0; i < 2; ++i)
						{
                            ItemRequirement requirement = trade.getItemRequirement(i);
                            if(requirement.test(item))
                                return true;
						}
					}
				}
			}
		}
		else
		{
			//Scan all trades for sale items to restock
            ItemTradeNode node = this.targets.getTraderNode(ItemTradeNode.TYPE);
			if(node != null)
			{
				for(ItemTradeData trade : node.getAllTrades())
				{
					if(trade.isSale() || trade.isBarter())
					{
						for(int i = 0; i < 2; ++i)
						{
                            ItemRequirement requirement = trade.getItemRequirement(i);
                            if(requirement.test(item))
								return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public boolean allowOutput(ItemStack item) { return !this.allowInput(item); }

	public boolean isItemRelevant(ItemStack item) {
		if(this.getInteractionType().trades())
		{
			for(TradeReference<ItemTradeData> t : this.targets.getTradeReferences())
			{
				TradeData t2 = t.getLocalTrade();
				if(t2 instanceof ItemTradeData trade && trade.allowItemInStorage(item))
					return true;
			}
		}
		else
		{
            ItemTradeNode node = this.targets.getTraderNode(ItemTradeNode.TYPE);
			if(node != null)
			{
				for(ItemTradeData trade : node.getAllTrades())
				{
					if(trade.allowItemInStorage(item))
						return true;
				}
			}
		}
		return false;
	}

	public int getStorageStackLimit() {
		return ItemTraderData.DEFAULT_STACK_LIMIT + CapacityUpgrade.getBonusCapacity(this.getUpgrades(),Upgrades.ITEM_CAPACITY);
	}

	@Override
	protected void saveAdditional(CompoundTag compound,DataContext<Tag> context) {
		super.saveAdditional(compound,context);
        this.itemBuffer.save(compound, "Storage",context);
	}
	
	public void setItemBufferDirty() {
        this.setChanged(builder -> builder.setList("Storage",this.itemBuffer.getContents(),ModLazyPackets.ITEM_STACK));
	}
	
	@Override
	public void loadAdditional(CompoundTag compound,DataContext<Tag> context) {
		super.loadAdditional(compound,context);
		if(compound.contains("Storage"))
			this.itemBuffer.load(compound.getCompound("Storage"),context);
	}

	@Override
	public boolean validTraderType(TraderData trader) { return trader instanceof ItemTraderData; }
	
	protected final ItemTraderData getItemTrader() {
		TraderData trader = this.targets.getTrader();
		if(trader instanceof ItemTraderData)
			return (ItemTraderData)trader;
		return null;
	}
	
	@Override
	protected void drainTick(TraderData trader) {
        ItemTradeNode node = trader.getNode(ItemTradeNode.TYPE);
        ItemStorageNode storageNode = trader.getNode(ItemStorageNode.TYPE);
		if(node != null && storageNode != null && trader.hasPermission(this.owner.getPlayerForContext(), Permissions.INTERACTION_LINK))
		{
			for(int i = 0; i < trader.getTradeCount(); ++i)
			{
				ItemTradeData trade = node.getTrade(i);
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
							int drainableAmount = storageNode.getStorage().getItemCount(drainItem);
							if(drainableAmount > 0)
							{
								ItemStack movingStack = drainItem.copy();
								movingStack.setCount(Math.min(movingStack.getMaxStackSize(), drainableAmount));
								//Remove the stack from storage
								ItemStack removed = storageNode.getStorage().removeItemUnlimited(movingStack);
								//InventoryUtil.RemoveItemCount(trader.getStorage(), movingStack);
								//Put the stack in the item buffer (if possible)
								ItemStack leftovers = ItemHandlerHelper.insertItemStacked(this.itemBuffer, removed, false);
								//If some items couldn't be put in the item buffer, put them back in storage
								if(!leftovers.isEmpty())
                                    storageNode.getStorage().forceAddItem(leftovers);
							}
						}
					}
				}
			}
		}
	}

	@Override
	protected void restockTick(TraderData trader) {
        ItemTradeNode node = trader.getNode(ItemTradeNode.TYPE);
        ItemStorageNode storageNode = trader.getNode(ItemStorageNode.TYPE);
		if(node != null && storageNode != null && trader.hasPermission(this.owner.getPlayerForContext(), Permissions.INTERACTION_LINK))
		{
			for(int i = 0; i < trader.getTradeCount(); ++i)
			{
				ItemTradeData trade = node.getTrade(i);
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
								ItemStack removedItem = this.itemBuffer.removeItemUnlimited(movingStack);
								if(removedItem.getCount() == movingStack.getCount())
								{
                                    storageNode.getStorage().tryAddItem(movingStack);
									if(!movingStack.isEmpty())
									{
										//Place the leftovers back in storage
										this.itemBuffer.forceAddItem(movingStack);
									}
								}
								else
									this.itemBuffer.forceAddItem(removedItem);
							}
						}
					}
				}
			}
		}
	}

	@Override
	protected void tradeTick(TradeReference<ItemTradeData> tr) {
		TradeData t = tr.getTrueTrade();
		if(t instanceof ItemTradeData trade)
		{
			if(trade != null && trade.isValid())
			{
				if(trade.isSale())
				{
					//Confirm that we have enough space to store the purchased item(s)
					if(this.itemBuffer.canFitItems(trade.getSellItem(0), trade.getSellItem(1)))
					{
						if(this.TryExecuteTrade(tr).isSuccess())
							this.setItemBufferDirty();
					}
				}
				else if(trade.isPurchase())
				{
					//Confirm that we have enough of the item in storage to sell the item(s)
					if(this.itemBuffer.hasItems(trade.getSellItem(0), trade.getSellItem(1)))
					{
						if(this.TryExecuteTrade(tr).isSuccess())
							this.setItemBufferDirty();
					}
				}
				else if(trade.isBarter())
				{
					//Confirm that we have enough space to store the purchased item AND
					//That we have enough of the item in storage to barter away.
					if(this.itemBuffer.hasItems(trade.getBarterItem(0), trade.getBarterItem(1)) && this.itemBuffer.canFitItems(trade.getSellItem(0), trade.getSellItem(1)))
					{
						if(this.TryExecuteTrade(tr).isSuccess())
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
			if(this.itemHandler.allowInputSide(relativeSide) || this.itemHandler.allowOutputSide(relativeSide))
			{
				Direction actualSide = relativeSide;
				if(this.getBlockState().getBlock() instanceof IRotatableBlock b)
					actualSide = IRotatableBlock.getActualSide(b.getFacing(this.getBlockState()), relativeSide);
				
				BlockPos queryPos = this.worldPosition.relative(actualSide);
				IItemHandler itemHandler = this.level.getCapability(Capabilities.ItemHandler.BLOCK, queryPos, actualSide.getOpposite());
				if(itemHandler != null)
				{
					//Collect items from neighboring blocks
					if(this.itemHandler.allowInputSide(relativeSide))
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
					if(this.itemHandler.allowOutputSide(relativeSide)) {
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
										this.itemBuffer.removeItemUnlimited(stack);
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
    protected void handleSyncPacket(LazyPacketData data) {
        super.handleSyncPacket(data);
        //Update storage
        if(data.contains("Storage"))
            this.itemBuffer.load(data.getList("Storage",ModLazyPackets.ITEM_STACK));
    }
}
