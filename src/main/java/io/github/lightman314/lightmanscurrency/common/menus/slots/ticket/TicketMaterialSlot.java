package io.github.lightman314.lightmanscurrency.common.menus.slots.ticket;

import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class TicketMaterialSlot extends Slot{
	
	public TicketMaterialSlot(Container inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean mayPlace(@Nonnull ItemStack stack) { return InventoryUtil.ItemHasTag(stack, LCTags.Items.TICKET_MATERIAL); }

}
