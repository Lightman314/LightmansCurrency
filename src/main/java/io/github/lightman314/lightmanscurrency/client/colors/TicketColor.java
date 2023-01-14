package io.github.lightman314.lightmanscurrency.client.colors;

import java.util.UUID;

import io.github.lightman314.lightmanscurrency.items.TicketItem;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TicketColor implements ItemColor{

	@Override
	public int getColor(@NotNull ItemStack itemStack, int color) {
		
		if(color > 0)
			return 0xFFFFFF;
		
		//Get the Ticket's ID
		UUID id = TicketItem.GetTicketID(itemStack);
		if(id == null)
			return 0xFFFFFF;
		//Make admin tickets yellow (because I can muahahahaha)
		if(id.equals(TicketItem.CREATIVE_TICKET_ID))
			return 0xFFFF00;
		//Convert the ID into a color
		int hash = id.hashCode();
		//Ensure that the hash code is a positive number
		if(hash < 0)
			hash *= -1;
		//Limit to a valid color id
		return hash % 0xFFFFFF;
	}
	
}
