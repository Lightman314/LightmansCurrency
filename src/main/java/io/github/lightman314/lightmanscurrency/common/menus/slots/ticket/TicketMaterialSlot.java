package io.github.lightman314.lightmanscurrency.common.menus.slots.ticket;

import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class TicketMaterialSlot extends Slot {
	
	public TicketMaterialSlot(IInventory inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean mayPlace(@Nonnull ItemStack stack) { return InventoryUtil.ItemHasTag(stack, TicketItem.TICKET_MATERIAL_TAG); }

}
