package io.github.lightman314.lightmanscurrency.items;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.core.ModItems;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class TicketItem extends Item{

	public TicketItem(Properties properties)
	{
		super(properties);
	}
	
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
	{
		super.appendHoverText(stack,  worldIn,  tooltip,  flagIn);
		if(isMasterTicket(stack))
		{
			tooltip.add(new TranslatableComponent("tooltip.lightmanscurrency.ticket.master"));
		}
		if(Screen.hasShiftDown())
		{
			UUID ticketID = GetTicketID(stack);
			if(ticketID != null)
				tooltip.add(new TranslatableComponent("tooltip.lightmanscurrency.ticket.id", ticketID));
		}
	}
	
	public static boolean isMasterTicket(ItemStack ticket)
	{
		if(ticket.isEmpty() || ticket.getItem() != ModItems.TICKET || !ticket.hasTag())
			return false;
		CompoundTag ticketTag = ticket.getTag();
		if(!ticketTag.contains("TicketID"))
			return false;
		if(ticketTag.contains("Master"))
			return ticketTag.getBoolean("Master");
		else
			return false;
	}
	
	public static UUID GetTicketID(ItemStack ticket)
	{
		//Get the ticket item
		if(ticket.isEmpty() || ticket.getItem() != ModItems.TICKET || !ticket.hasTag())
			return null;
		CompoundTag ticketTag = ticket.getTag();
		if(!ticketTag.contains("TicketID"))
			return null;
		return ticketTag.getUUID("TicketID");
	}
	
}
