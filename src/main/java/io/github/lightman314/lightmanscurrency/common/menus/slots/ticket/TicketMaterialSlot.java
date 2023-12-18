package io.github.lightman314.lightmanscurrency.common.menus.slots.ticket;

import io.github.lightman314.lightmanscurrency.common.menus.TicketStationMenu;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class TicketMaterialSlot extends Slot{

	private final TicketStationMenu menu;

	public TicketMaterialSlot(TicketStationMenu menu, Container inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
		this.menu = menu;
	}
	
	@Override
	public boolean mayPlace(@Nonnull ItemStack stack) { return this.menu.getAllRecipes().stream().anyMatch(r -> r.validIngredient(stack)); }

}
