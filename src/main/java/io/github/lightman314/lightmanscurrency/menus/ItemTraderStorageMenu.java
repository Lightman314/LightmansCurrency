package io.github.lightman314.lightmanscurrency.menus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.ItemTraderStorageUtil;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.menus.containers.SuppliedContainer;
import io.github.lightman314.lightmanscurrency.menus.interfaces.IItemEditCapable;
import io.github.lightman314.lightmanscurrency.menus.interfaces.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.core.ModMenus;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.item_trader.MessageOpenItemEdit;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
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
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;

public class ItemTraderStorageMenu extends AbstractContainerMenu implements ITraderStorageMenu, IItemEditCapable{

	public static final int SCREEN_EXTENSION = ItemTraderStorageUtil.SCREEN_EXTENSION;
	
	public final Player player;
	
	private final Supplier<IItemTrader> traderSource;
	public IItemTrader getTrader() { return this.traderSource.get(); }
	
	final Container safeStorage;
	final Container coinSlots;
	
	public ItemTraderStorageMenu(int windowId, Inventory inventory, BlockPos traderPos) {
		this(ModMenus.ITEM_TRADER_STORAGE, windowId, inventory, () -> {
			BlockEntity be = inventory.player.level.getBlockEntity(traderPos);
			if(be instanceof IItemTrader)
				return (IItemTrader)be;
			return null;
		});
	}
	
	public ItemTraderStorageMenu(MenuType<?> type, int windowId, Inventory inventory, Supplier<IItemTrader> traderSource)
	{
		super(type, windowId);
		this.traderSource = traderSource;
		
		this.player = inventory.player;
		
		this.getTrader().userOpen(this.player);
		
		int tradeCount = this.getTrader().getTradeCount();
		int rowCount = ItemTraderStorageUtil.getRowCount(tradeCount);
		int columnCount = 9 * ItemTraderStorageUtil.getColumnCount(tradeCount);
		
		//Storage Slots
		this.safeStorage = new SuppliedContainer(new SafeStorageSupplier(this.traderSource));
		for(int y = 0; y < rowCount; y++)
		{
			for(int x = 0; x < columnCount && x + y * columnCount < this.safeStorage.getContainerSize(); x++)
			{
				this.addSlot(new Slot(this.safeStorage, x + y * columnCount, 8 + x * 18 + SCREEN_EXTENSION + ItemTraderStorageUtil.getStorageSlotOffset(tradeCount, y), 18 + y * 18));
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
		return (ItemTraderStorageUtil.getRowCount(this.getTrader().getTradeCount()) * 18) + 28;
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
			/*if(index < this.getTrader().getStorage().getContainerSize())
			{
				if(!this.moveItemStackTo(slotStack,  this.getTrader().getStorage().getContainerSize() + this.coinSlots.getContainerSize(), this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			//Merge items from the coin slots back into the players inventory
			else if(index < this.getTrader().getStorage().getContainerSize() + this.coinSlots.getContainerSize())
			{
				if(!this.moveItemStackTo(slotStack, this.getTrader().getStorage().getContainerSize() + this.coinSlots.getContainerSize(), this.slots.size(), true))
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
					if(!this.moveItemStackTo(slotStack, this.getTrader().getStorage().getContainerSize(), this.getTrader().getStorage().getContainerSize() + this.coinSlots.getContainerSize(), false))
					{
						return ItemStack.EMPTY;
					}
				}
				//Merge everything else into the storage slots
				else if(!this.moveItemStackTo(slotStack, 0, this.getTrader().getStorage().getContainerSize(), false))
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
			}*/
		}
		
		return clickedStack;
		
	}
	
	@Override
	public boolean stillValid(Player playerIn)
	{
		return this.getTrader() != null ? this.getTrader().hasPermission(playerIn, Permissions.OPEN_STORAGE) : false;
	}
	
	@Override
	public void removed(Player playerIn)
	{
		
		super.removed(playerIn);
		
		this.clearContainer(playerIn, coinSlots);
		
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
		if(this.player.level.isClientSide)
		{
			//LightmansCurrency.LogInfo("Attempting to open item edit container for tradeIndex " + tradeIndex);
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenItemEdit(tradeIndex));
		}
		else
		{
			//LightmansCurrency.LogInfo("Attempting to open item edit container for tradeIndex " + tradeIndex);
			//this.getTrader().openItemEditMenu(this.player, tradeIndex);
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
		this.getTrader().addStoredMoney(addValue);
		this.coinSlots.clearContent();
	}
	
	public void CollectCoinStorage()
	{
		if(this.getTrader() == null)
		{
			this.player.closeContainer();
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
		Container inventory = new SimpleContainer(coinList.size());
		for(int i = 0; i < coinList.size(); i++)
		{
			inventory.setItem(i, coinList.get(i));
		}
		this.clearContainer(player, inventory);
		
		//Clear the coin storage
		this.getTrader().clearStoredMoney();
		
	}
	
	//Menu Combination Functions/Types
	public static class ItemTraderStorageMenuUniversal extends ItemTraderStorageMenu
	{
		public ItemTraderStorageMenuUniversal(int windowID, Inventory inventory, UUID traderID) {
			super(ModMenus.ITEM_TRADER_STORAGE_UNIVERSAL, windowID, inventory, () ->{
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
	
	private class SafeStorageSupplier implements Supplier<Container>
	{

		private final Supplier<IItemTrader> traderSource;
		private final int storageSize;
		private final IItemTrader getTrader() { return this.traderSource.get(); }
		SafeStorageSupplier(Supplier<IItemTrader> traderSource)
		{
			this.traderSource = traderSource;
			this.storageSize = this.getTrader().getTradeCount() * 9;
		}
		
		@Override
		public Container get() {
			//IItemTrader trader = this.getTrader();
			//if(trader != null)
			//	return trader.getStorage();
			return new SimpleContainer(this.storageSize);
		}
		
	}
	
}
