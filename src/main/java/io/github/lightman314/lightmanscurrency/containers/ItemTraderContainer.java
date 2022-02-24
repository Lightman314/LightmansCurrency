package io.github.lightman314.lightmanscurrency.containers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.ItemTraderUtil;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderCashRegisterContainer;
import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderContainer;
import io.github.lightman314.lightmanscurrency.containers.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.core.ModContainers;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.tileentity.CashRegisterTileEntity;
import io.github.lightman314.lightmanscurrency.tileentity.TraderTileEntity;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class ItemTraderContainer extends Container implements ITraderContainer{
	
	public final PlayerEntity player;
	
	protected static final ContainerType<?> type = ModContainers.ITEM_TRADER;
	
	protected final IInventory coinSlots = new Inventory(5);
	protected final IInventory itemSlots = new Inventory(3);
	//protected final IInventory tradeDisplays;
	private final Supplier<IItemTrader> traderSource;
	public final IItemTrader getTrader() { return this.traderSource == null ? null : this.traderSource.get(); }
	
	public ItemTraderContainer(int windowId, PlayerInventory inventory, BlockPos traderPos)
	{
		this(ModContainers.ITEM_TRADER, windowId, inventory, traderPos);
	}
	
	protected ItemTraderContainer(ContainerType<?> type, int windowId, PlayerInventory inventory, BlockPos traderPos)
	{
		this(type, windowId, inventory, () -> {
			TileEntity te = inventory.player.world.getTileEntity(traderPos);
			if(te instanceof IItemTrader)
				return (IItemTrader)te;
			return null;
		});
	}
	
	protected ItemTraderContainer(ContainerType<?> type, int windowId, PlayerInventory inventory, Supplier<IItemTrader> traderSource)
	{
		super(type, windowId);
		this.traderSource = traderSource;
		
		this.player = inventory.player;
		
		this.getTrader().userOpen(this.player);
		
		//int tradeCount = this.getTradeCount();
		
		//Coin Slots
		for(int x = 0; x < coinSlots.getSizeInventory(); x++)
		{
			this.addSlot(new CoinSlot(this.coinSlots, x, ItemTraderUtil.getInventoryDisplayOffset(this.getTrader()) + 8 + (x + 4) * 18, getCoinSlotHeight()));
		}
		
		//Item Slots
		for(int x = 0; x < itemSlots.getSizeInventory(); x++)
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
	public boolean canInteractWith(PlayerEntity playerIn) { return this.getTrader() != null; }
	
	@Override
	public void onContainerClosed(PlayerEntity playerIn)
	{
		//CurrencyMod.LOGGER.info("Closing a Trader Container");
		super.onContainerClosed(playerIn);
		this.clearContainer(playerIn,  playerIn.world,  this.coinSlots);
		this.clearContainer(playerIn, playerIn.world, this.itemSlots);
		
		if(this.getTrader() != null)
			this.getTrader().userClose(this.player);
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerEntity, int index)
	{
		
		ItemStack clickedStack = ItemStack.EMPTY;
		
		Slot slot = this.inventorySlots.get(index);
		
		if(slot != null && slot.getHasStack())
		{
			ItemStack slotStack = slot.getStack();
			clickedStack = slotStack.copy();
			if(index < this.coinSlots.getSizeInventory() + this.itemSlots.getSizeInventory())
			{
				if(!this.mergeItemStack(slotStack,  this.coinSlots.getSizeInventory() + this.itemSlots.getSizeInventory(), this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(index < this.inventorySlots.size())
			{
				if(MoneyUtil.isCoin(slotStack.getItem()))
				{
					//Merge coins into coin slots
					if(!this.mergeItemStack(slotStack, 0, this.coinSlots.getSizeInventory(), false))
					{
						return ItemStack.EMPTY;
					}
				}
				else
				{
					//Merge non-coins into item slots
					if(!this.mergeItemStack(slotStack, this.coinSlots.getSizeInventory(), this.coinSlots.getSizeInventory() + this.itemSlots.getSizeInventory(), false))
					{
						return ItemStack.EMPTY;
					}
				}
			}
			
			if(slotStack.isEmpty())
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else
			{
				slot.onSlotChanged();
			}
		}
		
		return clickedStack;
		
	}
	
	public long GetCoinValue()
	{
		long value = 0;
		for(int i = 0; i < coinSlots.getSizeInventory(); i++)
		{
			value += MoneyUtil.getValue(coinSlots.getStackInSlot(i));
		}
		ItemStack wallet = LightmansCurrency.getWalletStack(this.player);
		if(!wallet.isEmpty())
		{
			value += MoneyUtil.getValue(WalletItem.getWalletInventory(wallet));
		}
		//CurrencyMod.LOGGER.info("Coin value of the open trader is " + value);
		return value;
	}
	
	public IInventory GetItemInventory() { return itemSlots; }
	
	public ItemTradeData GetTrade(int tradeIndex)
	{
		return this.getTrader().getTrade(tradeIndex);
	}
	
	public void ExecuteTrade(int tradeIndex)
	{
		
		//LightmansCurrency.LOGGER.info("Executing trade at index " + tradeIndex);
		if(this.getTrader() == null)
		{
			this.player.closeScreen();
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
		CoinValue price = this.getTrader().runTradeCostEvent(this.player, tradeIndex).getCostResult();
		
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
							IInventory temp = new Inventory(1);
							temp.setInventorySlotContents(0, coins);
							this.clearContainer(this.player, this.player.world, temp);
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
			
			//Ignore editing internal storage if this is flagged as creative.
			if(!this.getTrader().getCoreSettings().isCreative())
			{
				//Put the item in storage
				InventoryUtil.TryPutItemStack(this.getTrader().getStorage(), trade.getSellItem());
				//Remove the coins from storage
				this.getTrader().removeStoredMoney(price);
			}
			
			//Push the post-trade event
			this.getTrader().runPostTradeEvent(this.player, tradeIndex, price);
			
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
			
			//Ignore editing internal storage if this is flagged as creative.
			if(!this.getTrader().getCoreSettings().isCreative())
			{
				//Put the item in storage
				InventoryUtil.TryPutItemStack(this.getTrader().getStorage(), trade.getBarterItem());
				//Remove the item from storage
				trade.RemoveItemsFromStorage(this.getTrader().getStorage());
			}
			
			//Push the post-trade event
			this.getTrader().runPostTradeEvent(this.player, tradeIndex, price);
			
		}
		
	}
	
	public void CollectCoinStorage()
	{
		if(this.getTrader() == null)
		{
			this.player.closeScreen();
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
				IInventory inventory = new Inventory(1);
				inventory.setInventorySlotContents(0, coinList.get(i));
				this.clearContainer(player, player.getEntityWorld(), inventory);
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
	
	public static class ItemTraderContainerUniversal extends ItemTraderContainer
	{
		public ItemTraderContainerUniversal(int windowID, PlayerInventory inventory, UUID traderID) {
			super(ModContainers.ITEM_TRADER_UNIVERSAL, windowID, inventory, () -> {
				UniversalTraderData data = null;
				if(inventory.player.world.isRemote)
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
	
	public static class ItemTraderContainerCR extends ItemTraderContainer implements ITraderCashRegisterContainer
	{
		
		private CashRegisterTileEntity cashRegister;
		
		public ItemTraderContainerCR(int windowID, PlayerInventory inventory, BlockPos traderPos, CashRegisterTileEntity cashRegister)
		{
			super(ModContainers.ITEM_TRADER_CR, windowID, inventory, traderPos);
			this.cashRegister = cashRegister;
		}
		
		@Override
		public boolean isCashRegister() { return true; }
		
		@Override
		public CashRegisterTileEntity getCashRegister() { return this.cashRegister; }
		
		private TraderTileEntity getTraderTE()
		{
			IItemTrader trader = super.getTrader();
			if(trader instanceof TraderTileEntity)
				return (TraderTileEntity)trader;
			return null;
		}
		
		@Override
		public int getThisCRIndex() { return this.cashRegister.getTraderIndex(this.getTraderTE()); }
		
		@Override
		public int getTotalCRSize() { return this.cashRegister.getPairedTraderSize(); }
		
		@Override
		public void OpenNextContainer(int direction) {
			int thisIndex = this.getThisCRIndex();
			this.cashRegister.OpenContainer(thisIndex, thisIndex + direction, direction, this.player);
		}
		
		@Override
		public void OpenContainerIndex(int index) {
			int previousIndex = index - 1;
			if(previousIndex < 0)
				previousIndex = this.cashRegister.getPairedTraderSize() - 1;
			this.cashRegister.OpenContainer(previousIndex, index, 1, this.player);
		}
		
	}
	
	public boolean isCashRegister() { return false; }
	
	public CashRegisterTileEntity getCashRegister() { return null; }
	
	public int getThisCRIndex() { return 0; }
	
	public int getTotalCRSize() { return 0; }
	
}
