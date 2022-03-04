package io.github.lightman314.lightmanscurrency.menus.slots;

import io.github.lightman314.lightmanscurrency.items.TicketItem;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
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
		return InventoryUtil.ItemHasTag(stack, TicketItem.TICKET_MATERIAL_TAG);
	}

}
