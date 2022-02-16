package io.github.lightman314.lightmanscurrency.menus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.ItemTraderUtil;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.menus.interfaces.ITraderCashRegisterMenu;
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
import io.github.lightman314.lightmanscurrency.blockentity.CashRegisterBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemTraderMenu extends AbstractContainerMenu implements ITraderMenu {
	
	public final Player player;
	
	protected final Container coinSlots = new SimpleContainer(5);
	protected final Container itemSlots = new SimpleContainer(3);
	
	private final Supplier<IItemTrader> traderSource;
	public final IItemTrader getTrader() { return this.traderSource == null? null : this.traderSource.get();  }
	
	public ItemTraderMenu(int windowId, Inventory inventory, BlockPos traderPos)
	{
		this(ModMenus.ITEM_TRADER, windowId, inventory, traderPos);
	}
	
	protected ItemTraderMenu(MenuType<?> type, int windowId, Inventory inventory, BlockPos traderPos)
	{
		this(type, windowId, inventory, () -> {
			BlockEntity be = inventory.player.level.getBlockEntity(traderPos);
			if(be instanceof IItemTrader)
				return (IItemTrader)be;
			return null;
		});
	}
	
	protected ItemTraderMenu(MenuType<?> type, int windowId, Inventory inventory, Supplier<IItemTrader> traderSource)
	{
		super(type, windowId);
		this.traderSource = traderSource;
		
		this.player = inventory.player;
		
		this.getTrader().userOpen(this.player);
		
		//int tradeCount = this.getTradeCount();
		
		//Coin Slots
		for(int x = 0; x < coinSlots.getContainerSize(); x++)
		{
			this.addSlot(new CoinSlot(this.coinSlots, x, ItemTraderUtil.getInventoryDisplayOffset(this.getTrader()) + 8 + (x + 4) * 18, getCoinSlotHeight()));
		}
		
		//Item Slots
		for(int x = 0; x < itemSlots.getContainerSize(); x++)
		{
			this.addSlot(new Slot(this.itemSlots, x, ItemTraderUtil.getInventoryDisplayOffset(this.getTrader()) + 8 + x * 18, getCoinSlotHeight()));
		}
		
		//Player inventory
		for(int y = 0; y < 3; y++)
		{
			for(int x = 0; x < 9; x++)
			{
				this.addSlot(new Slot(inventory, x + y * 9 + 9, ItemTraderUtil.getInventoryDisplayOffset(this.getTrader()) + 8 + x * 18, getPlayerInventoryStartHeight() + y * 18));
			}
		}
		//Player hotbar
		for(int x = 0; x < 9; x++)
		{
			this.addSlot(new Slot(inventory, x, ItemTraderUtil.getInventoryDisplayOffset(this.getTrader()) + 8 + x * 18, getPlayerInventoryStartHeight() + 58));
		}
		
	}
	
	public int getTradeCount()
	{
		return this.getTrader().getTradeCount();
	}
	
	protected int getTradeButtonBottom()
	{
		return ItemTraderUtil.getTradeDisplayHeight(this.getTrader());
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
	public boolean stillValid(Player playerIn) { return this.getTrader() != null; }
	
	@Override
	public void removed(Player playerIn)
	{
		//CurrencyMod.LOGGER.info("Closing a Trader Container");
		super.removed(playerIn);
		this.clearContainer(playerIn,  this.coinSlots);
		this.clearContainer(playerIn, this.itemSlots);
		
		if(this.getTrader() != null)
			this.getTrader().userClose(playerIn);
		
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
	
	public ItemTradeData GetTrade(int tradeIndex)
	{
		return this.getTrader().getTrade(tradeIndex);
	}
	
	public void ExecuteTrade(int tradeIndex)
	{
		
		//LightmansCurrency.LOGGER.info("Executing trade at index " + tradeIndex);
		if(this.getTrader() == null)
		{
			this.player.closeContainer();
			return;
		}
		
		ItemTradeData trade = this.getTrader().getTrade(tradeIndex);
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
		if(this.getTrader().runPreTradeEvent(this.player, tradeIndex).isCanceled())
			return;
		
		//Get the cost of the trade
		CoinValue price = this.getTrader().runTradeCostEvent(player, tradeIndex).getCostResult();
		
		//Process a sale
		if(trade.isSale())
		{
			//Abort if not enough items in inventory
			if(!trade.hasStock(this.getTrader()) && !this.getTrader().getCoreSettings().isCreative())
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
			this.getTrader().getLogger().AddLog(player, trade, price, this.getTrader().getCoreSettings().isCreative());
			this.getTrader().markLoggerDirty();
			
			//Ignore editing internal storage if this is flagged as creative.
			if(!this.getTrader().getCoreSettings().isCreative())
			{
				//Remove the sold items from storage
				//InventoryUtil.RemoveItemCount(this.tileEntity, trade.getSellItem());
				trade.RemoveItemsFromStorage(this.getTrader().getStorage());
				//Give the paid cost to storage
				this.getTrader().addStoredMoney(price);
				
				this.getTrader().markStorageDirty();
				
			}
			
			//Push the post-trade event
			this.getTrader().runPostTradeEvent(this.player, tradeIndex, price);
			
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
			if(!trade.hasSpace(this.getTrader()) && !this.getTrader().getCoreSettings().isCreative())
			{
				LightmansCurrency.LogDebug("Not enough room in storage to store the purchased items.");
				return;
			}
			//Abort if not enough money to pay them back
			if(!trade.hasStock(this.getTrader()) && !this.getTrader().getCoreSettings().isCreative())
			{
				LightmansCurrency.LogDebug("Not enough money in storage to pay for the purchased items.");
				return;
			}
			//Passed the checks. Take the item(s) from the input slot
			InventoryUtil.RemoveItemCount(this.itemSlots, trade.getSellItem());
			//Put the payment in the purchasers wallet, coin slot, etc.
			MoneyUtil.ProcessChange(this.coinSlots, this.player, price);
			
			//Log the successful trade
			this.getTrader().getLogger().AddLog(player, trade, price, this.getTrader().getCoreSettings().isCreative());
			this.getTrader().markLoggerDirty();
			
			//Push the post-trade event
			this.getTrader().runPostTradeEvent(this.player, tradeIndex, price);
			
			//Ignore editing internal storage if this is flagged as creative.
			if(!this.getTrader().getCoreSettings().isCreative())
			{
				//Put the item in storage
				InventoryUtil.TryPutItemStack(this.getTrader().getStorage(), trade.getSellItem());
				//Remove the coins from storage
				this.getTrader().removeStoredMoney(price);
				
				this.getTrader().markStorageDirty();
				
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
			if(!trade.hasSpace(this.getTrader()) && !this.getTrader().getCoreSettings().isCreative())
			{
				LightmansCurrency.LogDebug("Not enough room in storage to store the purchased items.");
				return;
			}
			//Abort if not enough items in inventory
			if(!trade.hasStock(this.getTrader()) && !this.getTrader().getCoreSettings().isCreative())
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
			this.getTrader().getLogger().AddLog(player, trade, CoinValue.EMPTY, this.getTrader().getCoreSettings().isCreative());
			this.getTrader().markLoggerDirty();
			
			//Push the post-trade event
			this.getTrader().runPostTradeEvent(this.player, tradeIndex, price);
			
			//Ignore editing internal storage if this is flagged as creative.
			if(!this.getTrader().getCoreSettings().isCreative())
			{
				//Put the item in storage
				InventoryUtil.TryPutItemStack(this.getTrader().getStorage(), trade.getBarterItem());
				//Remove the item from storage
				trade.RemoveItemsFromStorage(this.getTrader().getStorage());
				
				this.getTrader().markStorageDirty();
				
			}
			
		}
		
	}
	
	public void CollectCoinStorage()
	{
		if(this.getTrader() == null)
		{
			this.player.closeContainer();
			return;
		}
		if(!this.getTrader().hasPermission(this.player, Permissions.COLLECT_COINS))
		{
			Settings.PermissionWarning(this.player, "collect stored coins", Permissions.COLLECT_COINS);
			return;
		}
		if(this.getTrader().getCoreSettings().hasBankAccount())
			return;
		List<ItemStack> coinList = MoneyUtil.getCoinsOfValue(this.getTrader().getInternalStoredMoney());
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
		this.getTrader().clearStoredMoney();
		
	}
	
	public boolean hasPermission(String permission)
	{
		if(this.getTrader() != null)
			return this.getTrader().hasPermission(this.player, permission);
		return false;
	}
	
	public int getPermissionLevel(String permission)
	{
		if(this.getTrader() != null)
			return this.getTrader().getPermissionLevel(this.player, permission);
		return 0;
	}
	
	//Menu Combination Functions/Types
	public static class ItemTraderMenuUniversal extends ItemTraderMenu
	{
		public ItemTraderMenuUniversal(int windowID, Inventory inventory, UUID traderID) {
			super(ModMenus.ITEM_TRADER_UNIVERSAL, windowID, inventory, () ->{
				UniversalTraderData data = null;
				if(inventory.player.level.isClientSide)
					data = ClientTradingOffice.getData(traderID);
				else
					data = TradingOffice.getData(traderID);
				if(data instanceof IItemTrader)
					return (IItemTrader)data;
				return null;
			});
		}
		
		@Override
		public boolean isUniversal() { return true; }
		
	}
	
	public boolean isUniversal() { return false; }
	
	public static class ItemTraderMenuCR extends ItemTraderMenu implements ITraderCashRegisterMenu
	{
		
		private CashRegisterBlockEntity cashRegister;
		
		public ItemTraderMenuCR(int windowID, Inventory inventory, BlockPos traderPos, CashRegisterBlockEntity cashRegister)
		{
			super(ModMenus.ITEM_TRADER_CR, windowID, inventory, traderPos);
			this.cashRegister = cashRegister;
		}
		
		@Override
		public boolean isCashRegister() { return true; }
		
		@Override
		public CashRegisterBlockEntity getCashRegister() { return this.cashRegister; }
		
		private TraderBlockEntity getTraderBE()
		{
			IItemTrader trader = super.getTrader();
			if(trader instanceof TraderBlockEntity)
				return (TraderBlockEntity) trader;
			return null;
		}
		
		@Override
		public int getThisCRIndex() { return this.cashRegister.getTraderIndex(this.getTraderBE()); }
		
		@Override
		public int getTotalCRSize() { return this.cashRegister.getPairedTraderSize(); }
		
		@Override
		public void OpenNextContainer(int direction) {
			int thisIndex = this.cashRegister.getTraderIndex((TraderBlockEntity)this.getTrader());
			this.cashRegister.OpenContainer(thisIndex, thisIndex + direction, direction, this.player);
		}
		
		@Override
		public void OpenContainerIndex(int index) {
			int previousIndex = index-1;
			if(previousIndex < 0)
				previousIndex = this.cashRegister.getPairedTraderSize() - 1;
			this.cashRegister.OpenContainer(previousIndex, index, 1, this.player);
		}
		
	}
	
	public boolean isCashRegister() { return false; }
	
	public CashRegisterBlockEntity getCashRegister() { return null; }

	public int getThisCRIndex() { return 0; }
	
	public int getTotalCRSize() { return 0; }
	
}
