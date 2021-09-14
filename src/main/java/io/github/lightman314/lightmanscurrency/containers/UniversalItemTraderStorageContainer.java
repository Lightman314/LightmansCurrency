package io.github.lightman314.lightmanscurrency.containers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.ItemTradeButton;
import io.github.lightman314.lightmanscurrency.common.ItemTraderStorageUtil;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalItemTraderData;
import io.github.lightman314.lightmanscurrency.containers.interfaces.ICreativeTraderContainer;
import io.github.lightman314.lightmanscurrency.containers.interfaces.IItemEditCapable;
import io.github.lightman314.lightmanscurrency.containers.interfaces.IUniversalTraderStorageContainer;
import io.github.lightman314.lightmanscurrency.containers.inventories.SuppliedInventory;
import io.github.lightman314.lightmanscurrency.containers.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.containers.slots.TradeInputSlot;
import io.github.lightman314.lightmanscurrency.core.ModContainers;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.item_trader.MessageOpenItemEdit;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageSyncTrades;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageSyncStorage;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import io.github.lightman314.lightmanscurrency.util.WalletUtil;
import io.github.lightman314.lightmanscurrency.util.WalletUtil.PlayerWallets;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.ItemTradeData;

public class UniversalItemTraderStorageContainer extends UniversalContainer implements IUniversalTraderStorageContainer, ICreativeTraderContainer, IItemEditCapable{

	public static final int BUTTONSPACE = ItemTradeButton.WIDTH + 20;
	public static final int SCREEN_EXTENSION = BUTTONSPACE;
	
	final Container tradeInventory;
	final Container coinSlots;
	private Container storage;
	private Container copyStorage;
	final List<TradeInputSlot> tradeSlots;
	
	public UniversalItemTraderData getData()
	{
		if(this.getRawData() == null || !(this.getRawData() instanceof UniversalItemTraderData))
			return null;
		return (UniversalItemTraderData)this.getRawData();
	}
	
	public UniversalItemTraderStorageContainer(int windowId, Inventory inventory, UUID traderID, CompoundTag traderCompound)
	{
		super(ModContainers.UNIVERSAL_ITEMTRADERSTORAGE, windowId, traderID, inventory.player, traderCompound);
		
		//Init storage inventory as a supplied inventory
		this.storage = new SuppliedInventory(() -> this.getData().getStorage());
		
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
		
		this.tradeInventory = new SimpleContainer(tradeCount);
		this.tradeSlots = new ArrayList<>(tradeCount);
		//Trade Slots
		for(int y = 0; y < tradeInventory.getContainerSize(); y++)
		{
			ItemTradeData trade = getData().getTrade(y);
			TradeInputSlot newSlot = new TradeInputSlot(tradeInventory, y, ItemTraderStorageUtil.getTradeSlotPosX(tradeCount, y), ItemTraderStorageUtil.getTradeSlotPosY(tradeCount, y), trade, this.player);
			this.addSlot(newSlot);
			this.tradeSlots.add(newSlot);
			this.tradeInventory.setItem(y, trade.getSellItem());
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
	
	public UniversalItemTraderStorageContainer(int windowId, Inventory inventory, UUID traderID)
	{
		
		super(ModContainers.UNIVERSAL_ITEMTRADERSTORAGE, windowId, traderID, inventory.player);
		
		//Init storage inventory as a supplied inventory
		this.storage = new SuppliedInventory(() -> this.getData().getStorage());
		
		this.copyStorage = InventoryUtil.copyInventory(this.storage);
		
		//Storage Slots
		for(int y = 0; y < this.getData().getTradeCount(); y++)
		{
			for(int x = 0; x < 18 && x + y * 18 < this.storage.getContainerSize(); x++)
			{
				this.addSlot(new Slot(this.storage, x + y * 18, 8 + x * 18 + SCREEN_EXTENSION, 18 + y * 18));
			}
		}
		
		int tradeCount = this.getData().getTradeCount();
		
		this.tradeInventory = new SimpleContainer(tradeCount);
		this.tradeSlots = new ArrayList<>(tradeCount);
		//Trade Slots
		for(int y = 0; y < tradeInventory.getContainerSize(); y++)
		{
			ItemTradeData trade = getData().getTrade(y);
			TradeInputSlot newSlot = new TradeInputSlot(tradeInventory, y, ItemTraderStorageUtil.getTradeSlotPosX(tradeCount, y), ItemTraderStorageUtil.getTradeSlotPosY(tradeCount, y), trade, this.player);
			this.addSlot(newSlot);
			this.tradeSlots.add(newSlot);
			this.tradeInventory.setItem(y, trade.getSellItem());
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
	
	@Override
	public void clicked(int slotId, int dragType, ClickType clickType, Player player)
	{
		if(ItemTraderStorageContainer.slotClickOverride(slotId, dragType, clickType, player, this.slots, this))
			return;
		
		super.clicked(slotId, dragType, clickType, player);
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
		SyncTrades();
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
			if(index < this.storage.getContainerSize() + this.tradeInventory.getContainerSize())
			{
				if(!this.moveItemStackTo(slotStack,  this.storage.getContainerSize() + this.tradeInventory.getContainerSize() + this.coinSlots.getContainerSize(), this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			//Merge items from the coin slots back into the players inventory
			else if(index < this.storage.getContainerSize() + this.tradeInventory.getContainerSize() + this.coinSlots.getContainerSize())
			{
				LightmansCurrency.LogInfo("Merging coin slots back into inventory.");
				if(!this.moveItemStackTo(slotStack, this.storage.getContainerSize() + this.tradeInventory.getContainerSize() + this.coinSlots.getContainerSize(), this.slots.size(), true))
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
					if(!this.moveItemStackTo(slotStack, this.storage.getContainerSize() + this.tradeInventory.getContainerSize(), this.storage.getContainerSize() + this.tradeInventory.getContainerSize() + this.coinSlots.getContainerSize(), false))
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
	public boolean stillValid(Player playerIn)
	{
		return true;
	}
	
	@Override
	public void removed(Player playerIn)
	{
		
		this.clearContainer(playerIn, this.coinSlots);
		
		super.removed(playerIn);
		
	}
	
	
	
	/**
	 * Checks for changes to the trade inventory contents
	 */
	public void SyncTrades()
	{
		boolean changed = false;
		boolean isServer = !player.level.isClientSide;
		for(int i = 0; i < getData().getTradeCount() && !changed; i++)
		{
			if(!ItemStack.isSameItemSameTags(getData().getTrade(i).getSellItem(), this.tradeInventory.getItem(i)))
			{
				if(isServer)
					getData().getTrade(i).setSellItem(this.tradeInventory.getItem(i));
				changed = true;
			}
		}
		if(changed && isServer)
		{
			//Change detected server-side, so flag this trader's data as dirty (replaced tile entity update message)
			//LightmansCurrency.LOGGER.info("Server-side trade change detected. Flagging the data as dirty.");
			TradingOffice.MarkDirty(this.traderID);
		}
		else if(changed)
		{
			//Change was detected client-side, so inform the server that it needs to check for changes.
			//LightmansCurrency.LOGGER.info("Client-side trade change detected. Requesting the server to check for changes to the trades.");
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSyncTrades());
		}
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
			if(!ItemStack.isSameItemSameTags(this.storage.getItem(i), this.copyStorage.getItem(i)))
			{
				//LightmansCurrency.LOGGER.info("Storage change detected in slot " + i + ".");
				changed = true;
			}
		}
		if(changed && isServer)
		{
			//Change detected server-side, so flag this trader's data as dirty
			//LightmansCurrency.LOGGER.info("Server-side storage change detected. Flagging the data as dirty.");
			TradingOffice.MarkDirty(this.traderID);
			this.copyStorage = InventoryUtil.copyInventory(this.storage);
		}
		else if(changed)
		{
			//Change was detected client-side, so inform the server that it needs to check for changes.
			//LightmansCurrency.LOGGER.info("Client-side storage change detected. Requesting the server to check for changes to the trades.");
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSyncStorage());
			this.copyStorage = InventoryUtil.copyInventory(this.storage);
		}
	}
	
	/**
	 * Reloads the trade inventory contents from the trade data
	 */
	private void resyncTrades()
	{
		for(int i = 0; i < tradeInventory.getContainerSize(); i++)
		{
			ItemTradeData trade = this.getData().getTrade(i);
			if(trade != null)
			{
				tradeInventory.setItem(i, trade.getSellItem());
				tradeSlots.get(i).updateTrade(trade);
			}
			else
			{
				tradeInventory.setItem(i, ItemStack.EMPTY);
				tradeSlots.get(i).updateTrade(new ItemTradeData());
			}
		}
	}
	
	public boolean isOwner()
	{
		return getData().isOwner(player);
	}
	
	public void openItemEditScreenForSlot(int slotIndex)
	{
		int tradeIndex = slotIndex - this.storage.getContainerSize();
		openItemEditScreenForTrade(tradeIndex);
	}
	
	public void openItemEditScreenForTrade(int tradeIndex)
	{
		if(this.isClient())
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenItemEdit(tradeIndex));
		}
		else
			this.getData().openItemEditMenu(this.player, tradeIndex);
	}
	
	public boolean HasCoinsToAdd() { return !this.coinSlots.isEmpty(); }
	
	public void AddCoins()
	{
		if(this.getData() == null)
		{
			this.player.closeContainer();
			return;
		}
		//Get the value of the current 
		CoinValue addValue = CoinValue.easyBuild2(this.coinSlots);
		this.getData().addStoredMoney(addValue);
		this.coinSlots.clearContent();
	}
	
	public void CollectCoinStorage()
	{
		if(this.getData() == null)
		{
			this.player.closeContainer();
			return;
		}
		List<ItemStack> coinList = MoneyUtil.getCoinsOfValue(getData().getStoredMoney());
		PlayerWallets wallet = WalletUtil.getWallets(this.player);
		if(wallet.hasWallet())
		{
			List<ItemStack> spareCoins = new ArrayList<>();
			for(int i = 0; i < coinList.size(); i++)
			{
				ItemStack extraCoins = wallet.PlaceCoin(coinList.get(i));
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
	
	public void ToggleCreative()
	{
		if(this.getData() == null)
		{
			this.player.closeContainer();
			return;
		}
		this.getData().toggleCreative();
	}

	@Override
	protected void onDataModified() {
		this.resyncTrades();
		//Don't need to resync storage as it's already syncronized both client-side & server-side
	}

	@Override
	protected void onForceReopen() {
		LightmansCurrency.LogInfo("UniversalItemTraderStorageContainer.onForceReopen()");
		this.getData().openStorageMenu(this.player);
	}

	@Override
	public void AddTrade() {
		this.getData().addTrade();
	}

	@Override
	public void RemoveTrade() {
		this.getData().removeTrade();
	}
	
}
