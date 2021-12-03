package io.github.lightman314.lightmanscurrency.containers.inventories;

import net.minecraft.inventory.Inventory;

public class TicketInventory extends Inventory{

	public TicketInventory(int numSlots)
	{
		super(numSlots);
	}
	
	@Override
	public int getInventoryStackLimit() {
		return 1;
	}
	
}
