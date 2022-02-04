package io.github.lightman314.lightmanscurrency.menus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.common.ItemTraderUtil;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalItemTraderData;
import io.github.lightman314.lightmanscurrency.menus.interfaces.ITraderMenu;
import io.github.lightman314.lightmanscurrency.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.core.ModMenus;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class UniversalItemTraderMenu extends UniversalMenu implements ITraderMenu{
	
	
	protected static final MenuType<?> type = ModMenus.ITEMTRADER;
	
	protected final Container coinSlots = new SimpleContainer(5);
	protected final Container itemSlots = new SimpleContainer(3);
	//protected IInventory tradeDisplays;
	
	public UniversalItemTraderData getData()
	{
		if(this.getRawData() == null || !(this.getRawData() instanceof UniversalItemTraderData))
			return null;
		return (UniversalItemTraderData)this.getRawData();
	}
	
	public UniversalItemTraderMenu(int windowId, Inventory inventory, UUID traderID)
	{
		super(ModMenus.UNIVERSAL_ITEMTRADER, windowId, traderID, inventory.player);
		
		//int tradeCount = this.getTradeCount();
		
		//Coinslots
		for(int x = 0; x < coinSlots.getContainerSize(); x++)
		{
			this.addSlot(new CoinSlot(this.coinSlots, x, ItemTraderUtil.getInventoryDisplayOffset(this.getData()) + 8 + (x + 4) * 18, getCoinSlotHeight()));
		}
		
		//Item Output Slots
		for(int x = 0; x < itemSlots.getContainerSize(); x++)
		{
			this.addSlot(new Slot(this.itemSlots, x, ItemTraderUtil.getInventoryDisplayOffset(this.getData()) + 8 + x * 18, getCoinSlotHeight()));
		}
		
		//Player inventory
		for(int y = 0; y < 3; y++)
		{
			for(int x = 0; x < 9; x++)
			{
				this.addSlot(new Slot(inventory, x + y * 9 + 9, ItemTraderUtil.getInventoryDisplayOffset(this.getData()) + 8 + x * 18, getPlayerInventoryStartHeight() + y * 18));
			}
		}
		//Player hotbar
		for(int x = 0; x < 9; x++)
		{
			this.addSlot(new Slot(inventory, x, ItemTraderUtil.getInventoryDisplayOffset(this.getData()) + 8 + x * 18, getPlayerInventoryStartHeight() + 58));
		}
		
		/* Trade displays //Items are now rendered by the button.
		tradeDisplays = new Inventory(this.getData().getTradeCount());
		UpdateTradeDisplays();
		for(int i = 0; i < tradeCount; i++)
		{
			this.addSlot(new DisplaySlot(tradeDisplays, i, ItemTraderUtil.getSlotPosX(tradeCount, i), ItemTraderUtil.getSlotPosY(tradeCount, i)));
		}*/
		
	}
	
	public int getTradeCount()
	{
		return getData().getAllTrades().size();
	}
	
	protected int getTradeButtonBottom()
	{
		return ItemTraderUtil.getTradeDisplayHeight(this.getData()) + 11;
	}
	
	protected int getCoinSlotHeight()
	{
		return getTradeButtonBottom() + 8;
	}
	
	protected int getPlayerInventoryStartHeight()
	{
		return getCoinSlotHeight() + 32;
	}
	
	@Override
	public boolean stillValid(Player player) { return true; }
	
	@Override
	public void removed(Player playerIn)
	{
		//CurrencyMod.LOGGER.info("Closing a Trader Container");
		super.removed(playerIn);
		this.clearContainer(playerIn,  this.coinSlots);
		this.clearContainer(playerIn, this.itemSlots);
		
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
	
	public Container GetItemInventory() { return this.itemSlots; }
	
	public IItemTrader getTrader()
	{
		return this.getData();
	}
	
	public ItemTradeData GetTrade(int tradeIndex)
	{
		return this.getData().getTrade(tradeIndex);
	}
	
	public void ExecuteTrade(int tradeIndex)
	{
		ItemTradeData trade = getData().getTrade(tradeIndex);
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
		if(this.getData().runPreTradeEvent(this.player, tradeIndex).isCanceled())
			return;
		
		CoinValue price = this.getData().runTradeCostEvent(this.player, tradeIndex).getCostResult();
		
		//Execute a sale
		if(trade.isSale())
		{
			//Abort if not enough items in inventory
			if(!trade.hasStock(this.getData()) && !this.getData().getCoreSettings().isCreative())
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
			this.getData().getLogger().AddLog(player, trade, price, this.getData().getCoreSettings().isCreative());
			this.getData().markLoggerDirty();
			
			//Push the post-trade event
			this.getData().runPostTradeEvent(this.player, tradeIndex, price);
			
			//Ignore editing internal storage if this is flagged as creative.
			if(!this.getData().getCoreSettings().isCreative())
			{
				//Remove the sold items from storage
				//InventoryUtil.RemoveItemCount(this.getData().getStorage(), trade.getSellItem());
				trade.RemoveItemsFromStorage(this.getData().getStorage());
				//Give the payed cost to storage
				this.getData().addStoredMoney(price);
				this.getData().markStorageDirty();
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
			if(!InventoryUtil.CanPutItemStack(this.getData().getStorage(), trade.getSellItem()) && !this.getData().getCoreSettings().isCreative())
			{
				LightmansCurrency.LogDebug("Not enough room in storage to store the purchased items.");
			}
			//Abort if not enough money to pay them back
			if(!trade.hasStock(this.getData()) && !this.getData().getCoreSettings().isCreative())
			{
				LightmansCurrency.LogDebug("Not enough money in storage to pay for the purchased items.");
				return;
			}
			//Passed the checks. Take the item(s) from the input slot
			InventoryUtil.RemoveItemCount(this.itemSlots, trade.getSellItem());
			//Put the payment in the purchasers wallet, coin slot, etc.
			MoneyUtil.ProcessChange(this.coinSlots, this.player, price);
			
			//Log the successful trade
			this.getData().getLogger().AddLog(player, trade, price, this.getData().getCoreSettings().isCreative());
			this.getData().markLoggerDirty();
			
			//Push the post-trade event
			this.getData().runPostTradeEvent(this.player, tradeIndex, price);
			
			//Ignore editing internal storage if this is flagged as creative.
			if(!this.getData().getCoreSettings().isCreative())
			{
				//Put the item in storage
				InventoryUtil.TryPutItemStack(this.getData().getStorage(), trade.getSellItem());
				//Remove the coins from storage
				this.getData().removeStoredMoney(price);
				this.getData().markStorageDirty();
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
			if(!trade.hasSpace(this.getData()) && !this.getData().getCoreSettings().isCreative())
			{
				LightmansCurrency.LogDebug("Not enough room in storage to store the purchased items.");
				return;
			}
			//Abort if not enough items in inventory
			if(!trade.hasStock(this.getData()) && !this.getData().getCoreSettings().isCreative())
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
			this.getData().getLogger().AddLog(player, trade, CoinValue.EMPTY, this.getData().getCoreSettings().isCreative());
			this.getData().markLoggerDirty();
			
			//Push the post-trade event
			this.getData().runPostTradeEvent(this.player, tradeIndex, price);
			
			//Ignore editing internal storage if this is flagged as creative.
			if(!this.getData().getCoreSettings().isCreative())
			{
				//Put the item in storage
				InventoryUtil.TryPutItemStack(this.getData().getStorage(), trade.getBarterItem());
				//Remove the item from storage
				trade.RemoveItemsFromStorage(this.getData().getStorage());
				this.getData().markStorageDirty();
			}
			
		}
		
	}
	
	public void CollectCoinStorage()
	{
		if(!this.hasPermission(Permissions.COLLECT_COINS))
		{
			Settings.PermissionWarning(this.player, "collect stored coins", Permissions.COLLECT_COINS);
			return;
		}
		if(this.getData().getCoreSettings().hasBankAccount())
			return;
		List<ItemStack> coinList = MoneyUtil.getCoinsOfValue(this.getData().getStoredMoney());
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
		this.getData().clearStoredMoney();
		
	}
	
	public void tick()
	{
		//UpdateTradeDisplays();
	}

	@Override
	protected void onForceReopen() {
		LightmansCurrency.LogDebug("UniversalItemTraderContainer.onForceReopen()");
		this.getData().openTradeMenu(this.player);
	}
	
}
