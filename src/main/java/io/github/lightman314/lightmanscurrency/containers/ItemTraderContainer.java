package io.github.lightman314.lightmanscurrency.containers;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.interfaces.ITradeButtonContainer;
import io.github.lightman314.lightmanscurrency.common.ItemTraderUtil;
import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderContainer;
import io.github.lightman314.lightmanscurrency.containers.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.core.ModContainers;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.tileentity.ItemTraderTileEntity;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

public class ItemTraderContainer extends AbstractContainerMenu implements ITraderContainer, ITradeButtonContainer{
	
	public final Player player;
	
	protected final Container coinSlots = new SimpleContainer(5);
	protected final Container itemSlots = new SimpleContainer(3);
	//protected final IInventory tradeDisplays;
	public final ItemTraderTileEntity tileEntity;
	
	public ItemTraderContainer(int windowId, Inventory inventory, ItemTraderTileEntity tileEntity)
	{
		this(ModContainers.ITEMTRADER, windowId, inventory, tileEntity);
	}
	
	protected ItemTraderContainer(MenuType<?> type, int windowId, Inventory inventory, ItemTraderTileEntity tileEntity)
	{
		super(type, windowId);
		this.tileEntity = tileEntity;
		
		this.player = inventory.player;
		
		this.tileEntity.userOpen(this.player);
		
		//int tradeCount = this.getTradeCount();
		
		//Coin Slots
		for(int x = 0; x < coinSlots.getContainerSize(); x++)
		{
			this.addSlot(new CoinSlot(this.coinSlots, x, ItemTraderUtil.getInventoryDisplayOffset(tileEntity) + 8 + (x + 4) * 18, getCoinSlotHeight()));
		}
		
		//Item Slots
		for(int x = 0; x < itemSlots.getContainerSize(); x++)
		{
			this.addSlot(new Slot(this.itemSlots, x, ItemTraderUtil.getInventoryDisplayOffset(tileEntity) + 8 + x * 18, getCoinSlotHeight()));
		}
		
		//Player inventory
		for(int y = 0; y < 3; y++)
		{
			for(int x = 0; x < 9; x++)
			{
				this.addSlot(new Slot(inventory, x + y * 9 + 9, ItemTraderUtil.getInventoryDisplayOffset(tileEntity) + 8 + x * 18, getPlayerInventoryStartHeight() + y * 18));
			}
		}
		//Player hotbar
		for(int x = 0; x < 9; x++)
		{
			this.addSlot(new Slot(inventory, x, ItemTraderUtil.getInventoryDisplayOffset(tileEntity) + 8 + x * 18, getPlayerInventoryStartHeight() + 58));
		}
		
	}
	
	public int getTradeCount()
	{
		return tileEntity.getTradeCount();
	}
	
	protected int getTradeButtonBottom()
	{
		return ItemTraderUtil.getTradeDisplayHeight(this.tileEntity);
	}
	
	protected int getCoinSlotHeight()
	{
		return getTradeButtonBottom() + 19;
	}
	
	protected int getPlayerInventoryStartHeight()
	{
		return getCoinSlotHeight() + 32;
	}
	
	@Override
	public boolean stillValid(Player playerIn)
	{
		return true;
	}
	
	@Override
	public void removed(Player playerIn)
	{
		//CurrencyMod.LOGGER.info("Closing a Trader Container");
		super.removed(playerIn);
		this.clearContainer(playerIn,  this.coinSlots);
		this.clearContainer(playerIn, this.itemSlots);
		
		this.tileEntity.userClose(this.player);
	}

	@Override
	public ItemStack quickMoveStack(Player playerEntity, int index)
	{
		
		ItemStack clickedStack = ItemStack.EMPTY;
		
		Slot slot = this.slots.get(index);
		
		if(slot != null && slot.hasItem())
		{
			ItemStack slotStack = slot.getItem();
			clickedStack = slotStack.copy();
			if(index < this.coinSlots.getContainerSize() + this.itemSlots.getContainerSize())
			{
				if(!this.moveItemStackTo(slotStack,  this.coinSlots.getContainerSize() + this.itemSlots.getContainerSize(), this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(index < this.slots.size())
			{
				if(MoneyUtil.isCoin(slotStack.getItem()))
				{
					//Merge coins into coin slots
					if(!this.moveItemStackTo(slotStack, 0, this.coinSlots.getContainerSize(), false))
					{
						return ItemStack.EMPTY;
					}
				}
				else
				{
					//Merge non-coins into item slots
					if(!this.moveItemStackTo(slotStack, this.coinSlots.getContainerSize(), this.coinSlots.getContainerSize() + this.itemSlots.getContainerSize(), false))
					{
						return ItemStack.EMPTY;
					}
				}
			}
			
			if(slotStack.isEmpty())
			{
				slot.set(ItemStack.EMPTY);
			}
			else
			{
				slot.setChanged();
			}
		}
		
		return clickedStack;
		
	}
	
	public long GetCoinValue()
	{
		long value = 0;
		for(int i = 0; i < coinSlots.getContainerSize(); i++)
		{
			value += MoneyUtil.getValue(coinSlots.getItem(i));
		}
		ItemStack wallet = LightmansCurrency.getWalletStack(this.player);
		if(!wallet.isEmpty())
		{
			value += MoneyUtil.getValue(WalletItem.getWalletInventory(wallet));
		}
		//CurrencyMod.LOGGER.info("Coin value of the open trader is " + value);
		return value;
	}
	
	public Container GetItemInventory() { return itemSlots; }
	
	@Override
	public boolean PermissionToTrade(int tradeIndex, List<Component> denialOutput)
	{
		ItemTradeData trade = tileEntity.getTrade(tradeIndex);
		if(trade == null)
			return false;
		PreTradeEvent event = new PreTradeEvent(this.player, trade, this, () -> this.tileEntity);
		if(!event.isCanceled())
			this.tileEntity.beforeTrade(event);
		if(!event.isCanceled())
			trade.beforeTrade(event);
		if(!event.isCanceled())
			MinecraftForge.EVENT_BUS.post(event);
		
		if(denialOutput != null)
			event.getDenialReasons().forEach(reason -> denialOutput.add(reason));
		
		return !event.isCanceled();
	}
	
	public IItemTrader getTrader()
	{
		return this.tileEntity;
	}
	
	public ItemTradeData GetTrade(int tradeIndex)
	{
		return this.tileEntity.getTrade(tradeIndex);
	}
	
	public TradeCostEvent TradeCostEvent(ItemTradeData trade)
	{
		TradeCostEvent event = new TradeCostEvent(this.player, trade, this, () -> this.tileEntity);
		this.tileEntity.tradeCost(event);
		trade.tradeCost(event);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}
	
	private void PostTradeEvent(ItemTradeData trade, CoinValue pricePaid)
	{
		//Enclose post trade event to prevent the trader owner from not getting compensated for the trade.
		try {
			PostTradeEvent event = new PostTradeEvent(this.player, trade, this, () -> this.tileEntity, pricePaid);
			this.tileEntity.afterTrade(event);
			if(event.isDirty())
			{
				this.tileEntity.markRulesDirty();
				event.clean();
			}
			trade.afterTrade(event);
			if(event.isDirty())
			{
				this.tileEntity.markTradesDirty();
				event.clean();
			}
			MinecraftForge.EVENT_BUS.post(event);
		}
		catch(Exception e) { e.printStackTrace(); }
	}
	
	public void ExecuteTrade(int tradeIndex)
	{
		
		//LightmansCurrency.LOGGER.info("Executing trade at index " + tradeIndex);
		if(this.tileEntity.isRemoved())
		{
			this.player.closeContainer();
			return;
		}
		
		ItemTradeData trade = tileEntity.getTrade(tradeIndex);
		//Abort if the trade is null
		if(trade == null)
		{
			LightmansCurrency.LogError("Trade at index " + tradeIndex + " is null. Cannot execute trade!");
			return;
		}
		
		//Abort if the trade is not valid
		if(!trade.isValid())
		{
			LightmansCurrency.LogWarning("Trade at index " + tradeIndex + " is not a valid trade. Cannot execute trade.");
			return;
		}
		
		//Check if the player is allowed to do the trade
		if(!PermissionToTrade(tradeIndex, null))
			return;
		
		//Get the cost of the trade
		CoinValue price = this.TradeCostEvent(trade).getCostResult();
		
		//Process a sale
		if(trade.isSale())
		{
			//Abort if not enough items in inventory
			if(!trade.hasStock(this.tileEntity) && !this.tileEntity.isCreative())
			{
				LightmansCurrency.LogDebug("Not enough items in storage to carry out the trade at index " + tradeIndex + ". Cannot execute trade.");
				return;
			}
			
			//Abort if not enough room to put the sold item
			if(!InventoryUtil.CanPutItemStack(this.itemSlots, trade.getSellItem()))
			{
				LightmansCurrency.LogInfo("Not enough room for the output item. Aborting trade!");
				return;
			}
			
			if(!MoneyUtil.ProcessPayment(this.coinSlots, this.player, price))
			{
				LightmansCurrency.LogDebug("Not enough money is present for the trade at index " + tradeIndex + ". Cannot execute trade.");
				return;
			}
			
			//We have enough money, and the trade is valid. Execute the trade
			//Get the trade itemStack
			ItemStack giveStack = trade.getSellItem();
			//Give the trade item
			if(!InventoryUtil.PutItemStack(this.itemSlots, giveStack))//If there's not enough room to give the item to the output item, abort the trade
			{
				LightmansCurrency.LogError("Not enough room for the output item. Giving refund & aborting Trade!");
				//Give a refund
				List<ItemStack> refundCoins = MoneyUtil.getCoinsOfValue(price);
				ItemStack wallet = LightmansCurrency.getWalletStack(this.player);
				
				for(int i = 0; i < refundCoins.size(); i++)
				{
					ItemStack coins = refundCoins.get(i);
					if(!wallet.isEmpty())
					{
						coins = WalletItem.PickupCoin(wallet, coins);
					}
					if(!coins.isEmpty())
					{
						coins = InventoryUtil.TryPutItemStack(this.coinSlots, coins);
						if(!coins.isEmpty())
						{
							Container temp = new SimpleContainer(1);
							temp.setItem(0, coins);
							this.clearContainer(this.player, temp);
						}
					}
				}
				return;
			}
			
			//Log the successful trade
			this.tileEntity.getLogger().AddLog(player, trade, price, this.tileEntity.isCreative());
			this.tileEntity.markLoggerDirty();
			
			//Push the post-trade event
			PostTradeEvent(trade, price);
			
			//Ignore editing internal storage if this is flagged as creative.
			if(!this.tileEntity.isCreative())
			{
				//Remove the sold items from storage
				//InventoryUtil.RemoveItemCount(this.tileEntity, trade.getSellItem());
				trade.RemoveItemsFromStorage(this.tileEntity.getStorage());
				//Give the payed cost to storage
				tileEntity.addStoredMoney(price);
			}
			
		}
		//Process a purchase
		else if(trade.isPurchase())
		{
			//Abort if not enough items in the item slots
			if(InventoryUtil.GetItemCount(this.itemSlots, trade.getSellItem()) < trade.getSellItem().getCount())
			{
				LightmansCurrency.LogDebug("Not enough items in the item slots to make the purchase.");
				return;
			}
			
			//Abort if not enough room to store the purchased items (unless we're creative)
			if(!trade.hasSpace(this.tileEntity) && !this.tileEntity.isCreative())
			{
				LightmansCurrency.LogDebug("Not enough room in storage to store the purchased items.");
				return;
			}
			//Abort if not enough money to pay them back
			if(!trade.hasStock(this.tileEntity) && !this.tileEntity.isCreative())
			{
				LightmansCurrency.LogDebug("Not enough money in storage to pay for the purchased items.");
				return;
			}
			//Passed the checks. Take the item(s) from the input slot
			InventoryUtil.RemoveItemCount(this.itemSlots, trade.getSellItem());
			//Put the payment in the purchasers wallet, coin slot, etc.
			MoneyUtil.ProcessChange(this.coinSlots, this.player, price);
			
			//Log the successful trade
			this.tileEntity.getLogger().AddLog(player, trade, price, this.tileEntity.isCreative());
			this.tileEntity.markLoggerDirty();
			
			//Push the post-trade event
			PostTradeEvent(trade, price);
			
			//Ignore editing internal storage if this is flagged as creative.
			if(!this.tileEntity.isCreative())
			{
				//Put the item in storage
				InventoryUtil.TryPutItemStack(this.tileEntity.getStorage(), trade.getSellItem());
				//Remove the coins from storage
				this.tileEntity.removeStoredMoney(price);
			}
			
		}
		//Process a barter
		else if(trade.isBarter())
		{
			//Abort if not enough items in the item slots
			if(InventoryUtil.GetItemCount(this.itemSlots, trade.getBarterItem()) < trade.getBarterItem().getCount())
			{
				LightmansCurrency.LogDebug("Not enough items in the item slots to make the barter.");
				return;
			}
			//Abort if not enough room to store the purchased items (unless we're creative)
			if(!trade.hasSpace(this.tileEntity) && !this.tileEntity.isCreative())
			{
				LightmansCurrency.LogDebug("Not enough room in storage to store the purchased items.");
				return;
			}
			//Abort if not enough items in inventory
			if(!trade.hasStock(this.tileEntity) && !this.tileEntity.isCreative())
			{
				LightmansCurrency.LogDebug("Not enough items in storage to carry out the trade at index " + tradeIndex + ". Cannot execute trade.");
				return;
			}
			
			//Passed the checks. Take the item(s) from the input slot
			InventoryUtil.RemoveItemCount(this.itemSlots, trade.getBarterItem());
			//Check if there's room for the new items
			if(!InventoryUtil.CanPutItemStack(this.itemSlots, trade.getSellItem()))
			{
				//Abort if no room for the sold item
				LightmansCurrency.LogDebug("Not enough room for the output item. Aborting trade!");
				InventoryUtil.PutItemStack(this.itemSlots, trade.getBarterItem());
				return;
			}
			//Add the new item into the the item slots
			InventoryUtil.PutItemStack(this.itemSlots, trade.getSellItem());
			
			//Log the successful trade
			this.tileEntity.getLogger().AddLog(player, trade, CoinValue.EMPTY, this.tileEntity.isCreative());
			this.tileEntity.markLoggerDirty();
			
			//Push the post-trade event
			PostTradeEvent(trade, price);
			
			//Ignore editing internal storage if this is flagged as creative.
			if(!this.tileEntity.isCreative())
			{
				//Put the item in storage
				InventoryUtil.TryPutItemStack(this.tileEntity.getStorage(), trade.getBarterItem());
				//Remove the item from storage
				trade.RemoveItemsFromStorage(this.tileEntity.getStorage());
			}
			
		}
		
	}
	
	public void CollectCoinStorage()
	{
		if(this.tileEntity.isRemoved())
		{
			this.player.closeContainer();
			return;
		}
		List<ItemStack> coinList = MoneyUtil.getCoinsOfValue(tileEntity.getStoredMoney());
		ItemStack wallet = LightmansCurrency.getWalletStack(this.player);
		if(!wallet.isEmpty())
		{
			List<ItemStack> spareCoins = new ArrayList<>();
			for(int i = 0; i < coinList.size(); i++)
			{
				ItemStack extraCoins = WalletItem.PickupCoin(wallet, coinList.get(i));
				if(!extraCoins.isEmpty())
					spareCoins.add(extraCoins);
			}
			coinList = spareCoins;
		}
		for(int i = 0; i < coinList.size(); i++)
		{
			if(!InventoryUtil.PutItemStack(this.coinSlots, coinList.get(i)))
			{
				Container inventory = new SimpleContainer(1);
				inventory.setItem(0, coinList.get(i));
				this.clearContainer(player, inventory);
			}
		}
		//Clear the coin storage
		tileEntity.clearStoredMoney();
		
	}
	
	public void tick()
	{
		if(this.tileEntity.isRemoved())
		{
			this.player.closeContainer();
			return;
		}
	}
	
	public boolean isOwner()
	{
		return tileEntity.isOwner(player);
	}
	
	public boolean hasPermissions()
	{
		return tileEntity.hasPermissions(player);
	}
	
}
