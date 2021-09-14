package io.github.lightman314.lightmanscurrency.containers;

import io.github.lightman314.lightmanscurrency.containers.slots.MintSlot;
import io.github.lightman314.lightmanscurrency.containers.slots.OutputSlot;
import io.github.lightman314.lightmanscurrency.core.ModContainers;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.MintRecipe;
import io.github.lightman314.lightmanscurrency.Config;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IWorldPosCallable;

public class MintContainer extends Container{

	private final int SLOTCOUNT = 2;
	
	private final IInventory objectInputs = new Inventory(SLOTCOUNT);
	private final IWorldPosCallable callable;
	
	private static boolean canMint()
	{
		return Config.canMint();
	}
	
	private static boolean canMelt()
	{
		return Config.canMelt();
	}
	
	public MintContainer(int windowId, PlayerInventory inventory)
	{
		this(windowId, inventory, IWorldPosCallable.DUMMY);
	}
	
	public MintContainer(int windowId, PlayerInventory inventory, final IWorldPosCallable callable)
	{
		super(ModContainers.MINT, windowId);
		this.callable = callable;
		
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
			this.clearContainer(playerIn,  world,  this.objectInputs);
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
			if(index < this.objectInputs.getSizeInventory())
			{
				if(!this.mergeItemStack(slotStack, this.objectInputs.getSizeInventory(), this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(!this.mergeItemStack(slotStack, 0, this.objectInputs.getSizeInventory() - 1, false))
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
		ItemStack currentOutputSlot = objectInputs.getStackInSlot(1);
		if(currentOutputSlot.isEmpty())
			return 64;
		else if(currentOutputSlot.getItem() != mintOutput.getItem())
			return 0;
		return 64 - currentOutputSlot.getCount();	
	}
	
	public boolean isMeltInput()
	{
		return MoneyUtil.isCoin(objectInputs.getStackInSlot(0));
	}
	
	public ItemStack getMintOutput()
	{
		ItemStack mintInput = objectInputs.getStackInSlot(0);
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
			mintCount = objectInputs.getStackInSlot(0).getCount();
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
		if(objectInputs.getStackInSlot(1).isEmpty())
		{
			objectInputs.setInventorySlotContents(1, mintOutput);
		}
		else
		{
			objectInputs.getStackInSlot(1).setCount(objectInputs.getStackInSlot(1).getCount() + mintOutput.getCount());
		}
		
		//Remove the input item(s)
		objectInputs.getStackInSlot(0).setCount(objectInputs.getStackInSlot(0).getCount() - mintCount);
		
		//Job is done!
		
	}
	
}
