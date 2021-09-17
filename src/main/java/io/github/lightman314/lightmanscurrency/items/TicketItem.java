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
		if(Screen.hasShiftDown())
		{
			UUID ticketID = GetTicketID(stack);
			if(ticketID != null)
				tooltip.add(new TranslationTextComponent("tooltip.lightmanscurrency.ticket.id", ticketID));
		}
	}
	
	public static boolean isMasterTicket(ItemStack ticket)
	{
		if(ticket.isEmpty() || !ticket.hasTag())
			return false;
		return ticket.getItem() == ModItems.TICKET_MASTER;
	}
	
	public static UUID GetTicketID(ItemStack ticket)
	{
		//Get the ticket item
		if(ticket.isEmpty() || !(ticket.getItem() instanceof TicketItem) || !ticket.hasTag())
			return null;
		CompoundNBT ticketTag = ticket.getTag();
		if(!ticketTag.contains("TicketID"))
			return null;
		return ticketTag.getUniqueId("TicketID");
	}
	
	public static ItemStack CreateMasterTicket(UUID ticketID)
	{
		ItemStack ticket = new ItemStack(ModItems.TICKET_MASTER);
		ticket.getOrCreateTag().putUniqueId("TicketID", ticketID);
		return ticket;
	}
	
	public static ItemStack CreateTicket(UUID ticketID, int count)
	{
		ItemStack ticket = new ItemStack(ModItems.TICKET, count);
		ticket.getOrCreateTag().putUniqueId("TicketID", ticketID);
		return ticket;
	}
	
}
