package io.github.lightman314.lightmanscurrency.containers.slots;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TicketMaterialSlot extends Slot{
	
	public TicketMaterialSlot(Container inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean mayPlace(ItemStack stack) {
		return stack.getItem().getTags().contains(new ResourceLocation("lightmanscurrency", "ticket_material"));
	}

}
