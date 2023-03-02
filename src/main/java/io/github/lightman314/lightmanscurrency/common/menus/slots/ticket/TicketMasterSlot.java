package io.github.lightman314.lightmanscurrency.common.menus.slots.ticket;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TicketMasterSlot extends Slot{
	
	public TicketMasterSlot(Container inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean mayPlace(ItemStack stack) {
		return stack.getItem() == ModItems.TICKET_MASTER.get();
	}
	
	@Override
	public Pair<ResourceLocation,ResourceLocation> getNoItemIcon() {
		return Pair.of(InventoryMenu.BLOCK_ATLAS, TicketSlot.EMPTY_TICKET_SLOT);
	}

}
