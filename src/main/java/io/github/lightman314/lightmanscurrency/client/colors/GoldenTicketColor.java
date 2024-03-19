package io.github.lightman314.lightmanscurrency.client.colors;

import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GoldenTicketColor implements ItemColor{
	
	@Override
	public int getColor(@NotNull ItemStack itemStack, int color) {
		//Get the Ticket's Color
		if(color == 1)
			return TicketItem.GetTicketColor(itemStack);

		//N/A
		return 0xFFFFFF;
	}
	
}
