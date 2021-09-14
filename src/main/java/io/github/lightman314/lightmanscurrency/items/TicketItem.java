package io.github.lightman314.lightmanscurrency.items;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.core.ModItems;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class TicketItem extends Item{

	public TicketItem(Properties properties)
	{
		super(properties);
	}
	
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
	{
		super.addInformation(stack,  worldIn,  tooltip,  flagIn);
		if(isMasterTicket(stack))
		{
			tooltip.add(new TranslationTextComponent("tooltip.lightmanscurrency.ticket.master"));
		}
		if(Screen.hasShiftDown())
		{
			UUID ticketID = GetTicketID(stack);
			if(ticketID != null)
				tooltip.add(new TranslationTextComponent("tooltip.lightmanscurrency.ticket.id", ticketID));
		}
	}
	
	public static boolean isMasterTicket(ItemStack ticket)
	{
		if(ticket.isEmpty() || ticket.getItem() != ModItems.TICKET || !ticket.hasTag())
			return false;
		CompoundNBT ticketTag = ticket.getTag();
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
		CompoundNBT ticketTag = ticket.getTag();
		if(!ticketTag.contains("TicketID"))
			return null;
		return ticketTag.getUniqueId("TicketID");
	}
	
}
