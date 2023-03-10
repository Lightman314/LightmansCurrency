package io.github.lightman314.lightmanscurrency.client.colors;

import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class TicketColor implements IItemColor {

	@Override
	public int getColor(@Nonnull ItemStack itemStack, int color) {

		//Get the Ticket's Color
		if(color == 0)
			return TicketItem.GetTicketColor(itemStack);

		//Get the Ticket's Inverted Color
		if(color == 1)
			return 0xFFFFFF - TicketItem.GetTicketColor(itemStack);

		//N/A
		return 0xFFFFFF;
	}
	
}
