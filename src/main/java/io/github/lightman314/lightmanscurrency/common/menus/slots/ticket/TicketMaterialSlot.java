package io.github.lightman314.lightmanscurrency.common.menus.slots.ticket;

import io.github.lightman314.lightmanscurrency.api.misc.menus.slots.EasySlot;
import io.github.lightman314.lightmanscurrency.common.menus.TicketStationMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class TicketMaterialSlot extends EasySlot {

	private final TicketStationMenu menu;

	public TicketMaterialSlot(TicketStationMenu menu,IItemHandlerModifiable inventory,int x,int y)
	{
		super(inventory,1,x,y);
		this.menu = menu;
	}
	
	@Override
	public boolean mayPlace(ItemStack stack) { return this.menu.getRecipeList().stream().anyMatch(r -> r.validIngredient(stack)); }

}
