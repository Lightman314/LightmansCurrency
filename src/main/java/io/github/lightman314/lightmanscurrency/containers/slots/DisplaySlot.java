package io.github.lightman314.lightmanscurrency.containers.slots;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class DisplaySlot extends Slot {

	public DisplaySlot(IInventory inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean isItemValid(ItemStack item)
	{
		return false;
	}
	
	@Override
	public boolean canTakeStack(PlayerEntity player)
	{
		return false;
	}
	
	@Override
	public void putStack(@Nonnull ItemStack stack)
	{
		
	}
	
	@Override
	public ItemStack decrStackSize(int amount)
	{
		//Return nothing, as nothing can be taken
		return ItemStack.EMPTY;
	}
	
}
