package io.github.lightman314.lightmanscurrency.containers;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.common.ItemTraderStorageUtil;
import io.github.lightman314.lightmanscurrency.containers.interfaces.IItemEditCapable;
import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderStorageContainer;
import io.github.lightman314.lightmanscurrency.containers.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.core.ModContainers;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.item_trader.MessageOpenItemEdit;
import io.github.lightman314.lightmanscurrency.tileentity.ItemTraderTileEntity;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class ItemTraderStorageContainer extends Container implements ITraderStorageContainer, IItemEditCapable{

	public static final int SCREEN_EXTENSION = ItemTraderStorageUtil.SCREEN_EXTENSION;
	
	public final PlayerEntity player;
	
	public final ItemTraderTileEntity tileEntity;
	
	//final IInventory tradeInventory;
	final IInventory coinSlots;
	//final List<TradeInputSlot> tradeSlots;
	
	public ItemTraderStorageContainer(int windowId, PlayerInventory inventory, ItemTraderTileEntity tileEntity)
	{
		super(ModContainers.ITEMTRADERSTORAGE, windowId);
		this.tileEntity = tileEntity;
		
		this.player = inventory.player;
		
		this.tileEntity.userOpen(this.player);
		
		int tradeCount = this.tileEntity.getTradeCount();
		int rowCount = ItemTraderStorageUtil.getRowCount(tradeCount);
		int columnCount = 9 * ItemTraderStorageUtil.getColumnCount(tradeCount);
		
		//Storage Slots
		for(int y = 0; y < rowCount; y++)
		{
			for(int x = 0; x < columnCount && x + y * columnCount < tileEntity.getSizeInventory(); x++)
			{
				this.addSlot(new Slot(tileEntity, x + y * columnCount, 8 + x * 18 + SCREEN_EXTENSION + ItemTraderStorageUtil.getStorageSlotOffset(tradeCount, y), 18 + y * 18));
			}
		}
		
		/* Trade rendering is now handled by the fake trade buttons. Interactions will now be handled client-side as well.
		this.tradeInventory = new Inventory(tradeCount);
		this.tradeSlots = new ArrayList<>(tradeCount);
		//Trade Slots
		for(int y = 0; y < tradeInventory.getSizeInventory(); y++)
		{
			ItemTradeData trade = tileEntity.getTrade(y);
			TradeInputSlot newSlot = new TradeInputSlot(tradeInventory, y, ItemTraderStorageUtil.getTradeSlotPosX(tradeCount, y), ItemTraderStorageUtil.getTradeSlotPosY(tradeCount, y), trade, this.player);
			this.addSlot(newSlot);
			this.tradeSlots.add(newSlot);
			this.tradeInventory.setInventorySlotContents(y, trade.getSellItem());
		}*/
		
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
		return (ItemTraderStorageUtil.getRowCount(this.tileEntity.getTradeCount()) * 18) + 28;
	}
	
	public void tick()
	{
		if(this.tileEntity.isRemoved())
		{
			this.player.closeScreen();
			return;
		}
	}
	
	/*@Override //No longer need to override the slot clicking, as changes to the trades are now handled in the screen click events
	public ItemStack slotClick(int slotId, int dragType, ClickType clickType, PlayerEntity player)
	{
		
		if(slotClickOverride(slotId, dragType, clickType, player, this.inventorySlots, this))
		{
			this.detectAndSendChanges();
			return ItemStack.EMPTY;
		}
		
		return super.slotClick(slotId, dragType, clickType, player);
		
	}*/
	
	/*public static boolean slotClickOverride(int slotId, int dragType, ClickType clickType, PlayerEntity player, List<Slot> inventorySlots, @Nullable IItemEditCapable itemEditCapability)
	{
		//LightmansCurrency.LOGGER.info("ItemTraderStorageContainer.slotClick(" + slotId + ", " + dragType + ", " + clickType + ", " + player.getName().getString() + ")");
		if(slotId > 0 && slotId < inventorySlots.size())
		{
			Slot slot = inventorySlots.get(slotId);
			if(slot instanceof TradeInputSlot && clickType != ClickType.CLONE)
			{
				//LightmansCurrency.LOGGER.info("TradeInputSlot slot clicked at slotID " + slotId);
				if(clickType == ClickType.PICKUP && (dragType == 0 || dragType == 1))
				{
					TradeInputSlot tradeSlot = (TradeInputSlot)slot;
					IInventory inventory = slot.inventory;
					int index = slot.getSlotIndex();
					ItemStack tradeStack = inventory.getStackInSlot(index);
					ItemStack handStack = player.inventory.getItemStack();
					//Remove items from the trade
					if(handStack.isEmpty())
					{
						if(!tradeStack.isEmpty())
						{
							if(dragType == 0)
								inventory.setInventorySlotContents(index, ItemStack.EMPTY);
							else
								inventory.decrStackSize(index, tradeStack.getCount() / 2);
						}
						else if(itemEditCapability != null)
						{
							//Open the ItemEdit screen
							//LightmansCurrency.LogInfo("Attempting to open the item edit screen for slot index " + slotId);
							itemEditCapability.openItemEditScreenForSlot(slotId);
						}
					}
					//Add items to the trade
					else if(tradeSlot.isTradeItemValid(handStack))
					{
						if(tradeStack.isEmpty())
						{
							//Replace the stack in the inventory
							if(dragType == 0 || handStack.getCount() < 2)
								inventory.setInventorySlotContents(index, handStack.copy());
							else
							{
								ItemStack smallStack = handStack.copy();
								smallStack.setCount(1);
								inventory.setInventorySlotContents(index, smallStack);
							}
							
						}
						else if(InventoryUtil.ItemMatches(handStack, tradeStack) && tradeStack.getCount() < tradeStack.getMaxStackSize())
						{
							//Add to the count
							if(dragType == 0 && handStack.getCount() > 1)
							{
								tradeStack.setCount(MathUtil.clamp(tradeStack.getCount() + handStack.getCount(), 1, tradeStack.getMaxStackSize()));
								inventory.setInventorySlotContents(index, tradeStack);
							}
							else
							{
								tradeStack.grow(1);
								inventory.setInventorySlotContents(index, tradeStack);
							}
						}
						else
						{
							//Override the stack in the inventory
							if(dragType == 0 || handStack.getCount() < 2)
								inventory.setInventorySlotContents(index, handStack.copy());
							else
							{
								ItemStack smallStack = handStack.copy();
								smallStack.setCount(1);
								inventory.setInventorySlotContents(index, smallStack);
							}
						}
					}
				}
				//Otherwise do nothing
				return true;
			}
		}
		return false;
	}*/
	
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
			if(index < this.tileEntity.getSizeInventory())
			{
				if(!this.mergeItemStack(slotStack,  this.tileEntity.getSizeInventory() + this.coinSlots.getSizeInventory(), this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			//Merge items from the coin slots back into the players inventory
			else if(index < this.tileEntity.getSizeInventory() + this.coinSlots.getSizeInventory())
			{
				LightmansCurrency.LogInfo("Merging coin slots back into inventory.");
				if(!this.mergeItemStack(slotStack, this.tileEntity.getSizeInventory() + this.coinSlots.getSizeInventory(), this.inventorySlots.size(), true))
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
					if(!this.mergeItemStack(slotStack, this.tileEntity.getSizeInventory(), this.tileEntity.getSizeInventory() + this.coinSlots.getSizeInventory(), false))
					{
						return ItemStack.EMPTY;
					}
				}
				//Merge everything else into the storage slots
				else if(!this.mergeItemStack(slotStack, 0, this.tileEntity.getSizeInventory(), false))
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
		
		this.tileEntity.userClose(this.player);
		
	}
	
	public boolean hasPermissions(String permission)
	{
		return tileEntity.getCoreSettings().hasPermission(this.player, permission);
	}
	
	public int getPermissionLevel(String permission)
	{
		return tileEntity.getCoreSettings().getPermissionLevel(this.player, permission);
	}
	
	public void openItemEditScreenForTrade(int tradeIndex)
	{
		if(this.player.world.isRemote)
		{
			//LightmansCurrency.LogInfo("Attempting to open item edit container for tradeIndex " + tradeIndex);
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenItemEdit(tradeIndex));
		}
		else
		{
			//LightmansCurrency.LogInfo("Attempting to open item edit container for tradeIndex " + tradeIndex);
			this.tileEntity.openItemEditMenu(this.player, tradeIndex);
		}
	}
	
	public boolean HasCoinsToAdd()
	{
		return !coinSlots.isEmpty();
	}
	
	public void AddCoins()
	{
		if(this.tileEntity.isRemoved())
		{
			this.player.closeScreen();
			return;
		}
		//Get the value of the current 
		CoinValue addValue = CoinValue.easyBuild2(this.coinSlots);
		this.tileEntity.addStoredMoney(addValue);
		this.coinSlots.clear();
	}
	
	public void CollectCoinStorage()
	{
		if(this.tileEntity.isRemoved())
		{
			this.player.closeScreen();
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
		IInventory inventory = new Inventory(coinList.size());
		for(int i = 0; i < coinList.size(); i++)
		{
			inventory.setInventorySlotContents(i, coinList.get(i));
		}
		this.clearContainer(player, player.getEntityWorld(), inventory);
		
		//Clear the coin storage
		tileEntity.clearStoredMoney();
		
	}
	
}
