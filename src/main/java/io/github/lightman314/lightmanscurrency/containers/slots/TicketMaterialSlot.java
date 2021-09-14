package io.github.lightman314.lightmanscurrency.containers.slots;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class TicketMaterialSlot extends Slot{
	
	public TicketMaterialSlot(IInventory inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		return stack.getItem().getTags().contains(new ResourceLocation("lightmanscurrency", "ticket_material"));
	}

}
