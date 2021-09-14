package io.github.lightman314.lightmanscurrency.containers;

import io.github.lightman314.lightmanscurrency.containers.slots.MintSlot;
import io.github.lightman314.lightmanscurrency.containers.slots.OutputSlot;
import io.github.lightman314.lightmanscurrency.core.ModContainers;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.MintRecipe;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import io.github.lightman314.lightmanscurrency.Config;

public class MintContainer extends AbstractContainerMenu{

	private final int SLOTCOUNT = 2;
	
	private final Container objectInputs = new SimpleContainer(SLOTCOUNT);
	
	private static boolean canMint()
	{
		return Config.canMint();
	}
	
	private static boolean canMelt()
	{
		return Config.canMelt();
	}
	
	public MintContainer(int windowId, Inventory inventory)
	{
		super(ModContainers.MINT, windowId);
		
		//Slots
		this.addSlot(new MintSlot(this.objectInputs, 0, 56, 21, this));
		this.addSlot(new OutputSlot(this.objectInputs, 1, 116, 21));
		
		//Player inventory
		for(int y = 0; y < 3; y++)
		{
			for(int x = 0; x < 9; x++)
			{
				this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 56 + y * 18));
			}
		}
		//Player hotbar
		for(int x = 0; x < 9; x++)
		{
			this.addSlot(new Slot(inventory, x, 8 + x * 18, 114));
		}
	}
	
	@Override
	public boolean stillValid(Player playerIn)
	{
		return true;
		//return this.callable.applyOrElse((world,pos) -> playerIn.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0, true);
	}
	
	//@Override
	public void onContainerClosed(Player playerIn)
	{
		//super.onContainerClosed(playerIn);
		this.clearContainer(playerIn, this.objectInputs);
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
			if(index < this.objectInputs.getContainerSize())
			{
				if(!this.moveItemStackTo(slotStack, this.objectInputs.getContainerSize(), this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(!this.moveItemStackTo(slotStack, 0, this.objectInputs.getContainerSize() - 1, false))
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
	
	public boolean validMintInput()
	{
		return !getMintOutput().isEmpty();
	}
	
	public boolean validMintInput(ItemStack item)
	{
		if(canMint())
		{
			for(MintRecipe recipe : MoneyUtil.getMintRecipes())
			{
				if(recipe.validInput(item.getItem()))
				{
					return true;
				}
			}
		}
		if(canMelt())
		{
			for(MintRecipe recipe : MoneyUtil.getMeltRecipes())
			{
				if(recipe.validInput(item.getItem()))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public int validMintOutput()
	{
		//Determind how many more coins can fit in the output slot based on the input item
		ItemStack mintOutput = getMintOutput();
		ItemStack currentOutputSlot = objectInputs.getItem(1);
		if(currentOutputSlot.isEmpty())
			return 64;
		else if(currentOutputSlot.getItem() != mintOutput.getItem())
			return 0;
		return 64 - currentOutputSlot.getCount();	
	}
	
	public boolean isMeltInput()
	{
		return MoneyUtil.isCoin(objectInputs.getItem(0));
	}
	
	public ItemStack getMintOutput()
	{
		ItemStack mintInput = objectInputs.getItem(0);
		if(mintInput == ItemStack.EMPTY)
			return ItemStack.EMPTY;
		
		if(canMint())
		{
			for(MintRecipe recipe : MoneyUtil.getMintRecipes())
			{
				if(recipe.validInput(mintInput.getItem()))
				{
					return recipe.getOutput();
				}
			}
		}
		if(canMelt())
		{
			for(MintRecipe recipe : MoneyUtil.getMeltRecipes())
			{
				if(recipe.validInput(mintInput.getItem()))
				{
					return recipe.getOutput();
				}
			}
		}
		
		return ItemStack.EMPTY;
	}
	
	public void mintCoins(boolean fullStack)
	{
		//Ignore if no valid input is present
		if(!validMintInput())
			return;
		
		//Determine how many to mint based on the input count & whether a fullStack input was given.
		int mintCount = 1;
		if(fullStack)
		{
			mintCount = objectInputs.getItem(0).getCount();
		}
		
		//Confirm that the output slot has enough room for the expected outputs
		if(mintCount > validMintOutput())
			mintCount = validMintOutput();
		if(mintCount <= 0)
			return;
		
		//Get the output items
		ItemStack mintOutput = getMintOutput();
		mintOutput.setCount(mintCount);
		
		//Place the output item(s)
		if(objectInputs.getItem(1).isEmpty())
		{
			objectInputs.setItem(1, mintOutput);
		}
		else
		{
			objectInputs.getItem(1).setCount(objectInputs.getItem(1).getCount() + mintOutput.getCount());
		}
		
		//Remove the input item(s)
		objectInputs.getItem(0).setCount(objectInputs.getItem(0).getCount() - mintCount);
		
		//Job is done!
		
	}
	
}
