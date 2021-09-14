package io.github.lightman314.lightmanscurrency.containers.slots;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BlacklistSlot extends Slot {

	Container blacklistInventory;
	int blacklistIndex;
	
	public BlacklistSlot(Container inventory, int index, int x, int y, Container blacklistInventory, int blacklistIndex)
	{
		super(inventory, index, x, y);
		setBlacklist(blacklistInventory, blacklistIndex);
	}
	
	public void setBlacklist(Container blacklistInventory, int blacklistIndex)
	{
		this.blacklistInventory = blacklistInventory;
		this.blacklistIndex = blacklistIndex;
	}
	
	ItemStack getBlacklistItem()
	{
		return this.container.getItem(this.blacklistIndex);
	}
	
	@Override
	public boolean mayPlace(ItemStack item)
	{
		if(this.blacklistIndex >= 0)
			return item != this.getBlacklistItem();
		return true;
	}
	
}
