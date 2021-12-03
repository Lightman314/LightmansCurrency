package io.github.lightman314.lightmanscurrency.containers.slots;

import io.github.lightman314.lightmanscurrency.core.ModItems;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class TicketMasterSlot extends Slot{
	
	public TicketMasterSlot(IInventory inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
		this.setBackground(PlayerContainer.LOCATION_BLOCKS_TEXTURE, TicketSlot.EMPTY_TICKET_SLOT);
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		return stack.getItem() == ModItems.TICKET_MASTER;
	}

}
