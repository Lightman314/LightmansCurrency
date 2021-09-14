package io.github.lightman314.lightmanscurrency.containers;

import io.github.lightman314.lightmanscurrency.containers.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.core.ModContainers;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IWorldPosCallable;

public class ATMContainer extends Container{
	
	private final IInventory coinInput = new Inventory(9);
	private final IWorldPosCallable callable;
	
	public ATMContainer(int windowId, PlayerInventory inventory)
	{
		this(windowId, inventory, IWorldPosCallable.DUMMY);
	}
	
	public ATMContainer(int windowId, PlayerInventory inventory, final IWorldPosCallable callable)
	{
		super(ModContainers.ATM, windowId);
		this.callable = callable;
		
		//Coinslots
		for(int x = 0; x < coinInput.getSizeInventory(); x++)
		{
			this.addSlot(new CoinSlot(this.coinInput, x, 8 + x * 18, 98, false));
		}
		
		//Player inventory
		for(int y = 0; y < 3; y++)
		{
			for(int x = 0; x < 9; x++)
			{
				this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 130 + y * 18));
			}
		}
		//Player hotbar
		for(int x = 0; x < 9; x++)
		{
			this.addSlot(new Slot(inventory, x, 8 + x * 18, 188));
		}
	}
	
	@Override
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return this.callable.applyOrElse((world,pos) -> playerIn.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0, true);
	}
	
	@Override
	public void onContainerClosed(PlayerEntity playerIn)
	{
		super.onContainerClosed(playerIn);
		this.callable.consume((world,pos) ->
		{
			this.clearContainer(playerIn,  world,  this.coinInput);
		});
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
	
	//Button Input Codes:
	//100:Convert All Up
	//1:Copper -> Iron			-1:Iron -> Copper
	//2:Iron -> Gold			-2:Gold -> Iron
	//3:Gold -> Emerald			-3:Emerald -> Gold
	//4:Emerald -> Diamond		-4:Diamond -> Emerald
	//5:Diamond -> Netherite	-5: Netherite -> Diamond
	//-100: Convert all down
	public void ConvertCoins(int buttonInput)
	{
		///Converting Upwards
		//Converting All Upwards
		if(buttonInput == 100)
		{
			//Run two passes
			MoneyUtil.ConvertAllCoinsUp(this.coinInput);
		}
		//Copper to Iron
		else if(buttonInput == 1)
		{
			MoneyUtil.ConvertCoinsUp(this.coinInput, ModItems.COIN_COPPER);
		}
		//Iron to Gold
		else if(buttonInput == 2)
		{
			MoneyUtil.ConvertCoinsUp(this.coinInput, ModItems.COIN_IRON);
		}
		//Gold to Emerald
		else if(buttonInput == 3)
		{
			MoneyUtil.ConvertCoinsUp(this.coinInput, ModItems.COIN_GOLD);
		}
		//Emerald to Diamond
		else if(buttonInput == 4)
		{
			MoneyUtil.ConvertCoinsUp(this.coinInput, ModItems.COIN_EMERALD);
		}
		//Diamond to Netherite
		else if(buttonInput == 5)
		{
			MoneyUtil.ConvertCoinsUp(this.coinInput, ModItems.COIN_DIAMOND);
		}
		///Converting Downwards
		//Converting All Downwards
		else if(buttonInput == -100)
		{
			MoneyUtil.ConvertAllCoinsDown(this.coinInput);
		}
		//Netherite to Diamond
		else if(buttonInput == -5)
		{
			MoneyUtil.ConvertCoinsDown(this.coinInput, ModItems.COIN_NETHERITE);
		}
		//Netherite to Diamond
		if(buttonInput == -4)
		{
			MoneyUtil.ConvertCoinsDown(this.coinInput, ModItems.COIN_DIAMOND);
		}
		//Netherite to Diamond
		if(buttonInput == -3)
		{
			MoneyUtil.ConvertCoinsDown(this.coinInput, ModItems.COIN_EMERALD);
		}
		//Netherite to Diamond
		if(buttonInput == -2)
		{
			MoneyUtil.ConvertCoinsDown(this.coinInput, ModItems.COIN_GOLD);
		}
		//Netherite to Diamond
		if(buttonInput == -1)
		{
			MoneyUtil.ConvertCoinsDown(this.coinInput, ModItems.COIN_IRON);
		}
		
	}
	
}
