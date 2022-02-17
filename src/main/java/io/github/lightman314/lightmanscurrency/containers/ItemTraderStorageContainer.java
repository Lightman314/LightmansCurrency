package io.github.lightman314.lightmanscurrency.containers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.ItemTraderStorageUtil;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.containers.interfaces.IItemEditCapable;
import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderStorageContainer;
import io.github.lightman314.lightmanscurrency.containers.inventories.SuppliedInventory;
import io.github.lightman314.lightmanscurrency.containers.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.core.ModContainers;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.item_trader.MessageOpenItemEdit;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
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

public class ItemTraderStorageContainer extends Container implements ITraderStorageContainer, IItemEditCapable{

	public static final int SCREEN_EXTENSION = ItemTraderStorageUtil.SCREEN_EXTENSION;
	
	public final PlayerEntity player;
	
	private final Supplier<IItemTrader> traderSource;
	public IItemTrader getTrader() { return this.traderSource.get(); }
	
	final IInventory safeStorage;
	final IInventory coinSlots;
	
	public ItemTraderStorageContainer(int windowId, PlayerInventory inventory, BlockPos traderPos) {
		this(ModContainers.ITEM_TRADER_STORAGE, windowId, inventory, () ->{
			TileEntity te = inventory.player.world.getTileEntity(traderPos);
			if(te instanceof IItemTrader)
				return (IItemTrader)te;
			return null;
		});
	}
	
	protected ItemTraderStorageContainer(ContainerType<?> type, int windowId, PlayerInventory inventory, Supplier<IItemTrader> traderSource)
	{
		super(type, windowId);
		this.traderSource = traderSource;
		
		this.player = inventory.player;
		
		this.getTrader().userOpen(this.player);
		
		int tradeCount = this.getTrader().getTradeCount();
		int rowCount = ItemTraderStorageUtil.getRowCount(tradeCount);
		int columnCount = 9 * ItemTraderStorageUtil.getColumnCount(tradeCount);
		
		//Storage Slots
		this.safeStorage = new SuppliedInventory(new SafeStorageSupplier(this.traderSource));
		for(int y = 0; y < rowCount; y++)
		{
			for(int x = 0; x < columnCount && x + y * columnCount < this.safeStorage.getSizeInventory(); x++)
			{
				this.addSlot(new Slot(this.safeStorage, x + y * columnCount, 8 + x * 18 + SCREEN_EXTENSION + ItemTraderStorageUtil.getStorageSlotOffset(tradeCount, y), 18 + y * 18));
			}
		}
		
		int inventoryOffset = ItemTraderStorageUtil.getInventoryOffset(tradeCount);
		
		//Coin slots
		this.coinSlots = new Inventory(5);
		for(int i = 0; i < 5; i++)
		{
			this.addSlot(new CoinSlot(this.coinSlots, i, inventoryOffset + 176 + 8 + SCREEN_EXTENSION, getStorageBottom() + 3 + i * 18));
		}
		
		//Player inventory
		for(int y = 0; y < 3; y++)
		{
			for(int x = 0; x < 9; x++)
			{
				this.addSlot(new Slot(inventory, x + y * 9 + 9, inventoryOffset + 8 + x * 18 + SCREEN_EXTENSION, getStorageBottom() + 15 + y * 18));
			}
		}
		
		//Player hotbar
		for(int x = 0; x < 9; x++)
		{
			this.addSlot(new Slot(inventory, x, inventoryOffset + 8 + x * 18 + SCREEN_EXTENSION, getStorageBottom() + 15 + 58));
		}
		
	}
	
	public int getStorageBottom()
	{
		return (ItemTraderStorageUtil.getRowCount(this.getTrader().getTradeCount()) * 18) + 28;
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
			//Merge items from storage back into the players inventory
			if(index < this.getTrader().getStorage().getSizeInventory())
			{
				if(!this.mergeItemStack(slotStack,  this.getTrader().getStorage().getSizeInventory() + this.coinSlots.getSizeInventory(), this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			//Merge items from the coin slots back into the players inventory
			else if(index < this.getTrader().getStorage().getSizeInventory() + this.coinSlots.getSizeInventory())
			{
				LightmansCurrency.LogInfo("Merging coin slots back into inventory.");
				if(!this.mergeItemStack(slotStack, this.getTrader().getStorage().getSizeInventory() + this.coinSlots.getSizeInventory(), this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else
			{
				//Merging items from the players inventory
				if(MoneyUtil.isCoin(slotStack))
				{
					//Merge coins into the coin slots
					if(!this.mergeItemStack(slotStack, this.getTrader().getStorage().getSizeInventory(), this.getTrader().getStorage().getSizeInventory() + this.coinSlots.getSizeInventory(), false))
					{
						return ItemStack.EMPTY;
					}
				}
				//Merge everything else into the storage slots
				else if(!this.mergeItemStack(slotStack, 0, this.getTrader().getStorage().getSizeInventory(), false))
				{
					return ItemStack.EMPTY;
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
	
	@Override
	public boolean canInteractWith(PlayerEntity playerIn) { return this.getTrader() != null && this.getTrader().hasPermission(playerIn, Permissions.OPEN_STORAGE); }
	
	@Override
	public void onContainerClosed(PlayerEntity playerIn)
	{
		clearContainer(playerIn, playerIn.world, coinSlots);
		
		super.onContainerClosed(playerIn);
		
		if(this.getTrader() != null)
			this.getTrader().userClose(this.player);
		
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
	
	public void openItemEditScreenForTrade(int tradeIndex)
	{
		if(!this.hasPermission(Permissions.EDIT_TRADES))
		{
			Settings.PermissionWarning(this.player, "open item edit", Permissions.EDIT_TRADES);
			return;
		}
		if(this.player.world.isRemote)
		{
			//LightmansCurrency.LogInfo("Attempting to open item edit container for tradeIndex " + tradeIndex);
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenItemEdit(tradeIndex));
		}
		else
		{
			//LightmansCurrency.LogInfo("Attempting to open item edit container for tradeIndex " + tradeIndex);
			this.getTrader().openItemEditMenu(this.player, tradeIndex);
		}
	}
	
	public boolean HasCoinsToAdd()
	{
		return !coinSlots.isEmpty();
	}
	
	public void AddCoins()
	{
		if(this.getTrader() == null)
		{
			this.player.closeScreen();
			return;
		}
		if(!this.hasPermission(Permissions.STORE_COINS))
		{
			Settings.PermissionWarning(this.player,"store coins", Permissions.STORE_COINS);
			return;
		}
		//Get the value of the current 
		CoinValue addValue = CoinValue.easyBuild2(this.coinSlots);
		this.getTrader().addStoredMoney(addValue);
		this.coinSlots.clear();
	}
	
	public void CollectCoinStorage()
	{
		if(this.getTrader() == null)
		{
			this.player.closeScreen();
			return;
		}
		if(!this.hasPermission(Permissions.COLLECT_COINS))
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
		IInventory inventory = new Inventory(coinList.size());
		for(int i = 0; i < coinList.size(); i++)
		{
			inventory.setInventorySlotContents(i, coinList.get(i));
		}
		this.clearContainer(player, player.getEntityWorld(), inventory);
		
		//Clear the coin storage
		this.getTrader().clearStoredMoney();
		
	}
	
	//Menu combination Functions/Types
	public static class ItemTraderStorageContainerUniversal extends ItemTraderStorageContainer {
		
		public ItemTraderStorageContainerUniversal(int windowID, PlayerInventory inventory, UUID traderID) {
			super(ModContainers.ITEM_TRADER_STORAGE_UNIVERSAL, windowID, inventory, () ->{
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
	
	private class SafeStorageSupplier implements Supplier<IInventory>
	{
		private final Supplier<IItemTrader> traderSource;
		private final int storageSize;
		private final IItemTrader getTrader() { return this.traderSource.get(); }
		SafeStorageSupplier(Supplier<IItemTrader> traderSource)
		{
			this.traderSource = traderSource;
			this.storageSize = this.getTrader().getStorage().getSizeInventory();
		}
		
		@Override
		public IInventory get() {
			IItemTrader trader = this.getTrader();
			if(trader != null)
				return trader.getStorage();
			return new Inventory(this.storageSize);
		}
	}
	
}
