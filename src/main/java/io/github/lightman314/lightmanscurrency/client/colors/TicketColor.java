package io.github.lightman314.lightmanscurrency.client.colors;

import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TicketColor implements ItemColor{

	@Override
	public int getColor(@NotNull ItemStack itemStack, int color) {
		//Apparently these now require an alpha channel, so now adding 0xFF000000 to all colors to ensure alpha is always full
		//Get the Ticket's Color
		if(color == 0)
			return 0xFF000000 + TicketItem.GetTicketColor(itemStack);

		//Get the Ticket's Inverted Color
		if(color == 1)
			return 0xFFFFFFFF - TicketItem.GetTicketColor(itemStack);

		//N/A
		return 0xFFFFFFFF;
	}
	
}
