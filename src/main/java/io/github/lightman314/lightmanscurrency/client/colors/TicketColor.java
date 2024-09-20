package io.github.lightman314.lightmanscurrency.client.colors;

import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TicketColor implements ItemColor{

	@Override
	public int getColor(@NotNull ItemStack itemStack, int layer) {
		//Get the Ticket's Color
		if(layer == 0)
			return TicketItem.GetTicketColor(itemStack);

		//Get the Ticket's Inverted Color
		if(layer == 1)
			return 0xFFFFFF - TicketItem.GetTicketColor(itemStack);

		//N/A
		return 0xFFFFFF;
	}
	
}
