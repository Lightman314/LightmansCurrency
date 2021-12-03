package io.github.lightman314.lightmanscurrency.containers;

import io.github.lightman314.lightmanscurrency.containers.slots.MintSlot;
import io.github.lightman314.lightmanscurrency.containers.slots.OutputSlot;
import io.github.lightman314.lightmanscurrency.core.ModContainers;
import io.github.lightman314.lightmanscurrency.tileentity.CoinMintTileEntity;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class MintContainer extends Container{

	public final CoinMintTileEntity tileEntity;
	
	public MintContainer(int windowId, PlayerInventory inventory, CoinMintTileEntity tileEntity)
	{
		super(ModContainers.MINT, windowId);
		this.tileEntity = tileEntity;
		
		//Slots
		this.addSlot(new MintSlot(this.tileEntity.getStorage(), 0, 56, 21, this.tileEntity));
		this.addSlot(new OutputSlot(this.tileEntity.getStorage(), 1, 116, 21));
		
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
		return true;
	}
	
	@Override
	public void onContainerClosed(PlayerEntity playerIn)
	{
		super.onContainerClosed(playerIn);
		/*this.callable.consume((world,pos) ->
		{
			this.clearContainer(playerIn,  world,  this.objectInputs);
		});*/
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
			if(index < this.tileEntity.getStorage().getSizeInventory())
			{
				if(!this.mergeItemStack(slotStack, this.tileEntity.getStorage().getSizeInventory(), this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(!this.mergeItemStack(slotStack, 0, this.tileEntity.getStorage().getSizeInventory() - 1, false))
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
	
	public boolean isMeltInput()
	{
		return MoneyUtil.isCoin(this.tileEntity.getStorage().getStackInSlot(0));
	}
	
}
