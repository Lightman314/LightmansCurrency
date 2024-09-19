package io.github.lightman314.lightmanscurrency.client.colors;

import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class GoldenTicketColor implements ItemColor{
	
	@Override
	public int getColor(@Nonnull ItemStack itemStack, int layer) {
		//Get the Ticket's Color
		if(layer == 1)
			return 0xFF000000 + TicketItem.GetTicketColor(itemStack);
		//N/A
		return 0xFFFFFFFF;
	}
	
}
