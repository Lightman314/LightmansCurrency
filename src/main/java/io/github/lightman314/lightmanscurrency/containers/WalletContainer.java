package io.github.lightman314.lightmanscurrency.containers;

import io.github.lightman314.lightmanscurrency.containers.slots.BlacklistSlot;
import io.github.lightman314.lightmanscurrency.containers.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.containers.slots.DisplaySlot;
import io.github.lightman314.lightmanscurrency.core.ModContainers;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WalletContainer extends Container{
	
	private final int walletStackIndex;
	public int getWalletIndex()
	{
		return this.walletStackIndex;
	}
	
	private final PlayerInventory inventory;
	
	private ItemStack getWallet()
	{
		if(this.walletStackIndex < 0)
			return LightmansCurrency.getWalletStack(this.inventory.player);
		if(this.inventory == null)
			return ItemStack.EMPTY;
		return this.inventory.getStackInSlot(this.walletStackIndex);
	}
	
	private final IInventory coinInput;
	
	private WalletItem walletItem;
	public final ITextComponent title;
	
	boolean autoConvert = false;
	
	public WalletContainer(int windowId, PlayerInventory inventory, int walletStackIndex)
	{
		
		super(ModContainers.WALLET, windowId);
		
		MinecraftForge.EVENT_BUS.register(this);
		
		this.walletStackIndex = walletStackIndex;
		this.inventory = inventory;
		
		this.walletItem = (WalletItem)getWallet().getItem();
		this.title = this.getWallet().getDisplayName();
		
		this.coinInput = new Inventory(WalletItem.InventorySize(this.walletItem));
		NonNullList<ItemStack> walletInventory = WalletItem.getWalletInventory(getWallet());
		for(int i = 0; i < this.coinInput.getSizeInventory() && i < walletInventory.size(); i++)
		{
			this.coinInput.setInventorySlotContents(i, walletInventory.get(i));
		}
		
		//Coinslots
		for(int y = 0; (y * 9) < this.coinInput.getSizeInventory(); y++)
		{
			for(int x = 0; x < 9 && (x + y * 9) < this.coinInput.getSizeInventory(); x++)
			{
				this.addSlot(new CoinSlot(this.coinInput, x + y * 9, 8 + x * 18, 18 + y * 18));
			}
		}
		
		//Player inventory
		for(int y = 0; y < 3; y++)
		{
			for(int x = 0; x < 9; x++)
			{
				if((x + (y * 9) + 9) == this.walletStackIndex)
					this.addSlot(new DisplaySlot(inventory, x + y * 9 + 9, 8 + x * 18, 32 + (y + getRowCount()) * 18));
				else
					this.addSlot(new BlacklistSlot(inventory, x + y * 9 + 9, 8 + x * 18, 32 + (y + getRowCount()) * 18, this.inventory, this.walletStackIndex));
			}
		}
		
		//Player hotbar
		for(int x = 0; x < 9; x++)
		{
			if(x == this.walletStackIndex)
				this.addSlot(new DisplaySlot(inventory, x, 8 + x * 18, 90 + getRowCount() * 18));
			else
				this.addSlot(new BlacklistSlot(inventory, x, 8 + x * 18, 90 + getRowCount() * 18, this.inventory, this.walletStackIndex));
		}
		
		this.autoConvert = WalletItem.getAutoConvert(getWallet());
		
	}
	
	public int getRowCount()
	{
		return 1 + ((this.coinInput.getSizeInventory() - 1)/9);
	}
	
	public int getSlotCount()
	{
		return this.coinInput.getSizeInventory();
	}
	
	@Override
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return true;
	}
	
	@Override
	public void onContainerClosed(PlayerEntity playerIn)
	{
		
		super.onContainerClosed(playerIn);
		
		this.saveWalletContents();
		
		MinecraftForge.EVENT_BUS.unregister(this);
		
	}
	
	@SubscribeEvent
	public void onTick(WorldTickEvent event)
	{
		if(event.side.isClient() || event.phase != TickEvent.Phase.START)
			return;
		if(this.getWallet().isEmpty())
		{
			this.inventory.player.closeScreen();
			return;
		}
		this.saveWalletContents();
	}
	
	public void saveWalletContents()
	{
		//Write the bag contents back into the item stack
		NonNullList<ItemStack> walletInventory = NonNullList.withSize(WalletItem.InventorySize(this.walletItem), ItemStack.EMPTY);
		for(int i = 0; i < walletInventory.size() && i < this.coinInput.getSizeInventory(); i++)
		{
			walletInventory.set(i, this.coinInput.getStackInSlot(i));
		}
		WalletItem.putWalletInventory(this.getWallet(), walletInventory);
		if(this.autoConvert != WalletItem.getAutoConvert(getWallet()))
			WalletItem.toggleAutoConvert(getWallet());
	}

	public boolean canConvert()
	{
		return WalletItem.CanConvert(walletItem);
	}
	
	public boolean canPickup()
	{
		return WalletItem.CanPickup(walletItem);
	}
	
	public boolean getAutoConvert()
	{
		return this.autoConvert;//WalletItem.getAutoConvert(this.getWallet());
	}
	
	public void ToggleAutoConvert()
	{
		this.autoConvert = !this.autoConvert;
		//WalletItem.toggleAutoConvert(this.getWallet());
	}
	
	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerEntity, int index)
	{
		
		if(index + this.coinInput.getSizeInventory() == this.walletStackIndex)
			return ItemStack.EMPTY;
		
		ItemStack clickedStack = ItemStack.EMPTY;
		
		Slot slot = this.inventorySlots.get(index);
		
		if(slot != null && slot.getHasStack())
		{
			ItemStack slotStack = slot.getStack();
			clickedStack = slotStack.copy();
			if(index < this.coinInput.getSizeInventory())
			{
				if(MoneyUtil.isCoin(slotStack.getItem()))
				{
					if(!this.mergeItemStack(slotStack,  this.coinInput.getSizeInventory(), this.inventorySlots.size(), true))
					{
						return ItemStack.EMPTY;
					}
				}
			}
			else if(!this.mergeItemStack(slotStack, 0, this.coinInput.getSizeInventory(), false))
			{
				return ItemStack.EMPTY;
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
	
	public void ConvertCoins()
	{
		MoneyUtil.ConvertAllCoinsUp(this.coinInput);
		MoneyUtil.SortCoins(this.coinInput);
	}
	
	public ItemStack PickupCoins(ItemStack stack)
	{
		
		ItemStack returnValue = stack.copy();
		
		for(int i = 0; i < coinInput.getSizeInventory() && !returnValue.isEmpty(); i++)
		{
			ItemStack thisStack = coinInput.getStackInSlot(i);
			if(thisStack.isEmpty())
			{
				coinInput.setInventorySlotContents(i, returnValue.copy());
				returnValue = ItemStack.EMPTY;
			}
			else if(thisStack.getItem() == returnValue.getItem())
			{
				int amountToAdd = MathUtil.clamp(returnValue.getCount(), 0, thisStack.getMaxStackSize() - thisStack.getCount());
				thisStack.setCount(thisStack.getCount() + amountToAdd);
				returnValue.setCount(returnValue.getCount() - amountToAdd);
			}
		}
		
		if(this.autoConvert)
			ConvertCoins();
		
		return returnValue;
	}
	
}
