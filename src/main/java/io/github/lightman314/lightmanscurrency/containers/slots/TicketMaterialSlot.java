package io.github.lightman314.lightmanscurrency.containers.slots;

import io.github.lightman314.lightmanscurrency.items.TicketItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class TicketMaterialSlot extends Slot{
	
	public TicketMaterialSlot(IInventory inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		return stack.getItem().getTags().contains(TicketItem.TICKET_MATERIAL_TAG);
	}

}
