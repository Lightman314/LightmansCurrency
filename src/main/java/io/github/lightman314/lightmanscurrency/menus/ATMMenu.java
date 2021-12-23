package io.github.lightman314.lightmanscurrency.menus;

import io.github.lightman314.lightmanscurrency.core.ModContainers;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ATMMenu extends AbstractContainerMenu{
	
	private final Container coinInput = new SimpleContainer(9);
	
	public ATMMenu(int windowId, Inventory inventory)
	{
		super(ModContainers.ATM, windowId);
		
		//Coinslots
		for(int x = 0; x < coinInput.getContainerSize(); x++)
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
	public boolean stillValid(Player playerIn)
	{
		return true;
	}
	
	@Override
	public void removed(Player playerIn)
	{
		super.removed(playerIn);
		this.clearContainer(playerIn,  this.coinInput);
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
			if(index < this.coinInput.getContainerSize())
			{
				if(MoneyUtil.isCoin(slotStack.getItem()))
				{
					if(!this.moveItemStackTo(slotStack,  this.coinInput.getContainerSize(), this.slots.size(), true))
					{
						return ItemStack.EMPTY;
					}
				}
			}
			else if(!this.moveItemStackTo(slotStack, 0, this.coinInput.getContainerSize(), false))
			{
				return ItemStack.EMPTY;
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
