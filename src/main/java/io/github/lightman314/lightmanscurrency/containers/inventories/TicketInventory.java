package io.github.lightman314.lightmanscurrency.containers.inventories;

import net.minecraft.world.SimpleContainer;

public class TicketInventory extends SimpleContainer{

	public TicketInventory(int numSlots) { super(numSlots); }
	
	@Override
	public int getMaxStackSize() { return 1; }
	
}
