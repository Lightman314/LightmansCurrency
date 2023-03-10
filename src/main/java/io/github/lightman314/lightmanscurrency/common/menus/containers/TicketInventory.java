package io.github.lightman314.lightmanscurrency.common.menus.containers;

import net.minecraft.inventory.Inventory;

public class TicketInventory extends Inventory {

	public TicketInventory(int numSlots) { super(numSlots); }
	
	@Override
	public int getMaxStackSize() { return 1; }
	
}
