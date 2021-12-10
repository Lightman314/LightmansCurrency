package io.github.lightman314.lightmanscurrency.client.colors;

import java.util.UUID;

import io.github.lightman314.lightmanscurrency.items.TicketItem;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;

public class TicketColor implements ItemColor{

	@Override
	public int getColor(ItemStack itemStack, int color) {
		
		if(color > 0)
			return 0xFFFFFF;
		
		//Get the Ticket's ID
		UUID id = TicketItem.GetTicketID(itemStack);
		if(id == null)
			return 0xFFFFFF;
		//Convert the ID into a color
		int hash = id.hashCode();
		//Ensure that the hash code is a positive number
		if(hash < 0)
			hash *= -1;
		//Limit to a valid color id
		int output = hash % 0xFFFFFF;
		return output;
	}
	
}
