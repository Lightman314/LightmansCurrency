package io.github.lightman314.lightmanscurrency.menus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.ItemTradeButton;
import io.github.lightman314.lightmanscurrency.common.ItemTraderStorageUtil;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalItemTraderData;
import io.github.lightman314.lightmanscurrency.menus.interfaces.IItemEditCapable;
import io.github.lightman314.lightmanscurrency.menus.interfaces.IUniversalTraderStorageMenu;
import io.github.lightman314.lightmanscurrency.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.core.ModMenus;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.menus.containers.SuppliedContainer;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.item_trader.MessageOpenItemEdit;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageSyncStorage;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;

public class UniversalItemTraderStorageMenu extends UniversalMenu implements IUniversalTraderStorageMenu, IItemEditCapable{

	public static final int BUTTONSPACE = ItemTradeButton.WIDTH + 20;
	public static final int SCREEN_EXTENSION = BUTTONSPACE;
	
	final Container coinSlots;
	private Container storage;
	private Container copyStorage;
	
	public UniversalItemTraderData getData()
	{
		if(this.getRawData() == null || !(this.getRawData() instanceof UniversalItemTraderData))
			return null;
		return (UniversalItemTraderData)this.getRawData();
	}
	
	public UniversalItemTraderStorageMenu(int windowId, Inventory inventory, UUID traderID)
	{
		
		super(ModMenus.UNIVERSAL_ITEMTRADERSTORAGE, windowId, traderID, inventory.player);
		
		//Init storage inventory as a supplied inventory
		this.storage = new SuppliedContainer(() -> this.getData().getStorage());
		
		this.copyStorage = InventoryUtil.copyInventory(this.storage);
		int tradeCount = this.getData().getTradeCount();
		int rowCount = ItemTraderStorageUtil.getRowCount(tradeCount);
		int columnCount = 9 * ItemTraderStorageUtil.getColumnCount(tradeCount);
		
		//Storage Slots
		for(int y = 0; y < rowCount; y++)
		{
			for(int x = 0; x < columnCount && x + y * columnCount < this.storage.getContainerSize(); x++)
			{
				this.addSlot(new Slot(this.storage, x + y * columnCount, 8 + x * 18 + SCREEN_EXTENSION + ItemTraderStorageUtil.getStorageSlotOffset(tradeCount, y), 18 + y * 18));
			}
		}
		
		int inventoryOffset = ItemTraderStorageUtil.getInventoryOffset(tradeCount);
		
		//Coin slots
		this.coinSlots = new SimpleContainer(5);
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
		return (ItemTraderStorageUtil.getRowCount(this.getData().getTradeCount()) * 18) + 28;
	}
	
	public void tick()
	{
		if(this.getData() == null)
		{
			this.player.closeContainer();
			return;
		}
		//SyncTrades();
		CheckStorage();
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
			//Merge items from storage back into the players inventory
			if(index < this.storage.getContainerSize())
			{
				if(!this.moveItemStackTo(slotStack,  this.storage.getContainerSize() + this.coinSlots.getContainerSize(), this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			//Merge items from the coin slots back into the players inventory
			else if(index < this.storage.getContainerSize() + this.coinSlots.getContainerSize())
			{
				LightmansCurrency.LogInfo("Merging coin slots back into inventory.");
				if(!this.moveItemStackTo(slotStack, this.storage.getContainerSize() + this.coinSlots.getContainerSize(), this.slots.size(), true))
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
					if(!this.moveItemStackTo(slotStack, this.storage.getContainerSize(), this.storage.getContainerSize() + this.coinSlots.getContainerSize(), false))
					{
						return ItemStack.EMPTY;
					}
				}
				//Merge everything else into the storage slots
				else if(!this.moveItemStackTo(slotStack, 0, this.storage.getContainerSize(), false))
				{
					return ItemStack.EMPTY;
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
	
	@Override
	public boolean stillValid(Player playerIn) { return true; }
	
	@Override
	public void removed(Player playerIn)
	{
		
		clearContainer(playerIn, coinSlots);
		
		super.removed(playerIn);
		
	}
	
	/**
	 * Checks for changes to the trader's storage contents
	 */
	public void CheckStorage()
	{
		boolean changed = false;
		boolean isServer = !player.level.isClientSide;
		for(int i = 0; i < this.storage.getContainerSize() && !changed; i++)
		{
			if(!ItemStack.isSame(this.storage.getItem(i), this.copyStorage.getItem(i)))
			{
				changed = true;
			}
		}
		if(changed && isServer)
		{
			this.getData().markStorageDirty();
			this.copyStorage = InventoryUtil.copyInventory(this.storage);
		}
		else if(changed)
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSyncStorage());
			this.copyStorage = InventoryUtil.copyInventory(this.storage);
		}
	}
	
	public void openItemEditScreenForTrade(int tradeIndex)
	{
		if(!this.hasPermission(Permissions.EDIT_TRADES))
		{
			Settings.PermissionWarning(this.player, "open item edit", Permissions.EDIT_TRADES);
			return;
		}
		if(this.isClient())
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenItemEdit(tradeIndex));
		}
		else
			this.getData().openItemEditMenu(this.player, tradeIndex);
	}
	
	public void AddCoins()
	{
		if(this.getData() == null)
		{
			this.player.closeContainer();
			return;
		}
		if(!this.hasPermission(Permissions.STORE_COINS))
		{
			Settings.PermissionWarning(this.player, "store coins", Permissions.STORE_COINS);
			return;
		}
		//Get the value of the current 
		CoinValue addValue = CoinValue.easyBuild2(this.coinSlots);
		this.getData().addStoredMoney(addValue);
		this.coinSlots.clearContent();
	}
	
	public boolean HasCoinsToAdd()
	{
		return !coinSlots.isEmpty();
	}
	
	public void CollectCoinStorage()
	{
		if(this.getData() == null)
		{
			this.player.closeContainer();
			return;
		}
		if(!this.hasPermission(Permissions.COLLECT_COINS))
		{
			Settings.PermissionWarning(this.player, "collect stored coins", Permissions.COLLECT_COINS);
			return;
		}
		if(this.getData().getCoreSettings().hasBankAccount())
			return;
		List<ItemStack> coinList = MoneyUtil.getCoinsOfValue(getData().getStoredMoney());
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
		Container inventory = new SimpleContainer(coinList.size());
		for(int i = 0; i < coinList.size(); i++)
		{
			inventory.setItem(i, coinList.get(i));
		}
		this.clearContainer(player, inventory);
		
		//Clear the coin storage
		getData().clearStoredMoney();
		
	}

	@Override
	protected void onForceReopen() {
		LightmansCurrency.LogInfo("UniversalItemTraderStorageContainer.onForceReopen()");
		this.getData().openStorageMenu(this.player);
	}
	
}
