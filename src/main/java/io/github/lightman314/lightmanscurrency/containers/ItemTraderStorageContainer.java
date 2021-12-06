package io.github.lightman314.lightmanscurrency.containers;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.common.ItemTraderStorageUtil;
import io.github.lightman314.lightmanscurrency.containers.interfaces.ICreativeTraderContainer;
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
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;

public class ItemTraderStorageContainer extends AbstractContainerMenu implements ITraderStorageContainer, ICreativeTraderContainer, IItemEditCapable{

	public static final int SCREEN_EXTENSION = ItemTraderStorageUtil.SCREEN_EXTENSION;
	
	public final Player player;
	
	public final ItemTraderTileEntity tileEntity;
	
	//final IInventory tradeInventory;
	final Container coinSlots;
	//final List<TradeInputSlot> tradeSlots;
	
	public ItemTraderStorageContainer(int windowId, Inventory inventory, ItemTraderTileEntity tileEntity)
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
				this.addSlot(new Slot(this.tileEntity.getStorage(), x + y * columnCount, 8 + x * 18 + SCREEN_EXTENSION + ItemTraderStorageUtil.getStorageSlotOffset(tradeCount, y), 18 + y * 18));
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
		return (ItemTraderStorageUtil.getRowCount(this.tileEntity.getTradeCount()) * 18) + 28;
	}
	
	public void tick()
	{
		if(this.tileEntity.isRemoved())
		{
			this.player.closeContainer();
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
	public ItemStack quickMoveStack(Player playerEntity, int index)
	{
		
		ItemStack clickedStack = ItemStack.EMPTY;
		
		Slot slot = this.slots.get(index);
		
		if(slot != null && slot.hasItem())
		{
			ItemStack slotStack = slot.getItem();
			clickedStack = slotStack.copy();
			//Merge items from storage back into the players inventory
			if(index < this.tileEntity.getSizeInventory())
			{
				if(!this.moveItemStackTo(slotStack,  this.tileEntity.getStorage().getContainerSize() + this.coinSlots.getContainerSize(), this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			//Merge items from the coin slots back into the players inventory
			else if(index < this.tileEntity.getSizeInventory() + this.coinSlots.getContainerSize())
			{
				LightmansCurrency.LogInfo("Merging coin slots back into inventory.");
				if(!this.moveItemStackTo(slotStack, this.tileEntity.getStorage().getContainerSize() + this.coinSlots.getContainerSize(), this.slots.size(), true))
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
					if(!this.moveItemStackTo(slotStack, this.tileEntity.getStorage().getContainerSize(), this.tileEntity.getStorage().getContainerSize() + this.coinSlots.getContainerSize(), false))
					{
						return ItemStack.EMPTY;
					}
				}
				//Merge everything else into the storage slots
				else if(!this.moveItemStackTo(slotStack, 0, this.tileEntity.getStorage().getContainerSize(), false))
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
		
		super.removed(playerIn);
		
		this.clearContainer(playerIn, coinSlots);
		
		this.tileEntity.userClose(this.player);
		
	}
	
	public boolean isOwner()
	{
		return tileEntity.isOwner(player);
	}
	
	public boolean hasPermissions()
	{
		return tileEntity.hasPermissions(player);
	}
	
	public void openItemEditScreenForTrade(int tradeIndex)
	{
		if(this.player.level.isClientSide)
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
			this.player.closeContainer();
			return;
		}
		//Get the value of the current 
		CoinValue addValue = CoinValue.easyBuild2(this.coinSlots);
		this.tileEntity.addStoredMoney(addValue);
		this.coinSlots.clearContent();
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
		Container inventory = new SimpleContainer(coinList.size());
		for(int i = 0; i < coinList.size(); i++)
		{
			inventory.setItem(i, coinList.get(i));
		}
		this.clearContainer(player, inventory);
		
		//Clear the coin storage
		tileEntity.clearStoredMoney();
		
	}
	
	public void ToggleCreative()
	{
		if(this.tileEntity.isRemoved())
		{
			this.player.closeContainer();
			return;
		}
		this.tileEntity.toggleCreative();
	}

	@Override
	public void AddTrade() {
		this.tileEntity.addTrade();
	}

	@Override
	public void RemoveTrade() {
		this.tileEntity.removeTrade();
	}
	
}
