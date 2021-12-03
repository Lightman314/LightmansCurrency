package io.github.lightman314.lightmanscurrency.containers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.ItemTradeButton;
import io.github.lightman314.lightmanscurrency.common.ItemTraderStorageUtil;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalItemTraderData;
import io.github.lightman314.lightmanscurrency.containers.interfaces.ICreativeTraderContainer;
import io.github.lightman314.lightmanscurrency.containers.interfaces.IItemEditCapable;
import io.github.lightman314.lightmanscurrency.containers.interfaces.IUniversalTraderStorageContainer;
import io.github.lightman314.lightmanscurrency.containers.inventories.SuppliedInventory;
import io.github.lightman314.lightmanscurrency.containers.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.core.ModContainers;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.item_trader.MessageOpenItemEdit;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageSyncStorage;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class UniversalItemTraderStorageContainer extends UniversalContainer implements IUniversalTraderStorageContainer, ICreativeTraderContainer, IItemEditCapable{

	public static final int BUTTONSPACE = ItemTradeButton.WIDTH + 20;
	public static final int SCREEN_EXTENSION = BUTTONSPACE;
	
	//final IInventory tradeInventory;
	final IInventory coinSlots;
	private IInventory storage;
	private IInventory copyStorage;
	//final List<TradeInputSlot> tradeSlots;
	
	public UniversalItemTraderData getData()
	{
		if(this.getRawData() == null || !(this.getRawData() instanceof UniversalItemTraderData))
			return null;
		return (UniversalItemTraderData)this.getRawData();
	}
	
	public UniversalItemTraderStorageContainer(int windowId, PlayerInventory inventory, UUID traderID)
	{
		
		super(ModContainers.UNIVERSAL_ITEMTRADERSTORAGE, windowId, traderID, inventory.player);
		
		//Init storage inventory as a supplied inventory
		this.storage = new SuppliedInventory(() -> this.getData().getStorage());
		
		this.copyStorage = InventoryUtil.copyInventory(this.storage);
		int tradeCount = this.getData().getTradeCount();
		int rowCount = ItemTraderStorageUtil.getRowCount(tradeCount);
		int columnCount = 9 * ItemTraderStorageUtil.getColumnCount(tradeCount);
		
		//Storage Slots
		for(int y = 0; y < rowCount; y++)
		{
			for(int x = 0; x < columnCount && x + y * columnCount < this.storage.getSizeInventory(); x++)
			{
				this.addSlot(new Slot(this.storage, x + y * columnCount, 8 + x * 18 + SCREEN_EXTENSION + ItemTraderStorageUtil.getStorageSlotOffset(tradeCount, y), 18 + y * 18));
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
	
	/*@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickType, PlayerEntity player)
	{
		if(ItemTraderStorageContainer.slotClickOverride(slotId, dragType, clickType, player, this.inventorySlots, this))
			return ItemStack.EMPTY;
		
		return super.slotClick(slotId, dragType, clickType, player);
	}*/
	
	public int getStorageBottom()
	{
		return (ItemTraderStorageUtil.getRowCount(this.getData().getTradeCount()) * 18) + 28;
	}
	
	public void tick()
	{
		if(this.getData() == null)
		{
			this.player.closeScreen();
			return;
		}
		//SyncTrades();
		CheckStorage();
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
			if(index < this.storage.getSizeInventory())
			{
				if(!this.mergeItemStack(slotStack,  this.storage.getSizeInventory() + this.coinSlots.getSizeInventory(), this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			//Merge items from the coin slots back into the players inventory
			else if(index < this.storage.getSizeInventory() + this.coinSlots.getSizeInventory())
			{
				LightmansCurrency.LogInfo("Merging coin slots back into inventory.");
				if(!this.mergeItemStack(slotStack, this.storage.getSizeInventory() + this.coinSlots.getSizeInventory(), this.inventorySlots.size(), true))
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
					if(!this.mergeItemStack(slotStack, this.storage.getSizeInventory(), this.storage.getSizeInventory() + this.coinSlots.getSizeInventory(), false))
					{
						return ItemStack.EMPTY;
					}
				}
				//Merge everything else into the storage slots
				else if(!this.mergeItemStack(slotStack, 0, this.storage.getSizeInventory(), false))
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
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return true;
	}
	
	@Override
	public void onContainerClosed(PlayerEntity playerIn)
	{
		
		clearContainer(playerIn, playerIn.world, coinSlots);
		
		super.onContainerClosed(playerIn);
		
	}
	
	/**
	 * Checks for changes to the trader's storage contents
	 */
	public void CheckStorage()
	{
		boolean changed = false;
		boolean isServer = !player.world.isRemote;
		for(int i = 0; i < this.storage.getSizeInventory() && !changed; i++)
		{
			if(!ItemStack.areItemStacksEqual(this.storage.getStackInSlot(i), this.copyStorage.getStackInSlot(i)))
			{
				//LightmansCurrency.LOGGER.info("Storage change detected in slot " + i + ".");
				changed = true;
			}
		}
		if(changed && isServer)
		{
			//Change detected server-side, so flag this trader's data as dirty
			//LightmansCurrency.LOGGER.info("Server-side storage change detected. Flagging the data as dirty.");
			
			this.getData().markStorageDirty();
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
	/*private void resyncTrades()
	{
		for(int i = 0; i < tradeInventory.getSizeInventory(); i++)
		{
			ItemTradeData trade = this.getData().getTrade(i);
			if(trade != null)
			{
				tradeInventory.setInventorySlotContents(i, trade.getSellItem());
				tradeSlots.get(i).updateTrade(trade);
			}
			else
			{
				tradeInventory.setInventorySlotContents(i, ItemStack.EMPTY);
				tradeSlots.get(i).updateTrade(new ItemTradeData());
			}
		}
	}*/
	
	public boolean isOwner()
	{
		return getData().isOwner(player);
	}
	
	public boolean hasPermissions()
	{
		return getData().hasPermissions(player);
	}
	
	public void openItemEditScreenForSlot(int slotIndex)
	{
		int tradeIndex = slotIndex - this.storage.getSizeInventory();
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
	
	public void AddCoins()
	{
		if(this.getData() == null)
		{
			this.player.closeScreen();
			return;
		}
		//Get the value of the current 
		CoinValue addValue = CoinValue.easyBuild2(this.coinSlots);
		this.getData().addStoredMoney(addValue);
		this.coinSlots.clear();
	}
	
	public boolean HasCoinsToAdd()
	{
		return !coinSlots.isEmpty();
	}
	
	public void CollectCoinStorage()
	{
		if(this.getData() == null)
		{
			this.player.closeScreen();
			return;
		}
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
		IInventory inventory = new Inventory(coinList.size());
		for(int i = 0; i < coinList.size(); i++)
		{
			inventory.setInventorySlotContents(i, coinList.get(i));
		}
		this.clearContainer(player, player.getEntityWorld(), inventory);
		
		//Clear the coin storage
		getData().clearStoredMoney();
		
	}
	
	public void ToggleCreative()
	{
		if(this.getData() == null)
		{
			this.player.closeScreen();
			return;
		}
		this.getData().toggleCreative();
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
