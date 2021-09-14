package io.github.lightman314.lightmanscurrency.containers.slots;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class BlacklistSlot extends Slot {

	IInventory blacklistInventory;
	int blacklistIndex;
	
	public BlacklistSlot(IInventory inventory, int index, int x, int y, IInventory blacklistInventory, int blacklistIndex)
	{
		super(inventory, index, x, y);
		setBlacklist(blacklistInventory, blacklistIndex);
	}
	
	public void setBlacklist(IInventory blacklistInventory, int blacklistIndex)
	{
		this.blacklistInventory = blacklistInventory;
		this.blacklistIndex = blacklistIndex;
	}
	
	ItemStack getItem()
	{
		return this.inventory.getStackInSlot(this.blacklistIndex);
	}
	
	@Override
	public boolean isItemValid(ItemStack item)
	{
		if(this.blacklistIndex >= 0)
			return item != this.getItem();
		return true;
	}
	
}
