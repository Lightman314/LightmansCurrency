package io.github.lightman314.lightmanscurrency.common.items;

import java.util.List;

import javax.annotation.Nonnull;

import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.items.data.TicketData;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class TicketItem extends Item{


	public TicketItem(Properties properties) { super(properties); }

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @Nonnull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn)
	{
		if(isPass(stack))
			tooltip.add(LCText.TOOLTIP_PASS.get());
		long ticketID = GetTicketID(stack);
		if(ticketID >= -2)
			tooltip.add(LCText.TOOLTIP_TICKET_ID.get(ticketID));
	}

	public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
		GetTicketID(stack);
	}

	public static boolean isTicket(ItemStack ticket)
	{
		if(ticket.isEmpty() || !ticket.has(ModDataComponents.TICKET_DATA))
			return false;
		return ticket.getItem() instanceof TicketItem && InventoryUtil.ItemHasTag(ticket, LCTags.Items.TICKETS_TICKET);
	}

	public static boolean isPass(ItemStack ticket)
	{
		if(ticket.isEmpty() || !ticket.has(ModDataComponents.TICKET_DATA))
			return false;
		return ticket.getItem() instanceof TicketItem && InventoryUtil.ItemHasTag(ticket, LCTags.Items.TICKETS_PASS);
	}

	public static boolean isTicketOrPass(ItemStack ticket)  { return isTicket(ticket) || isPass(ticket); }

	public static boolean isMasterTicket(ItemStack ticket)
	{
		if(ticket.isEmpty() || !ticket.has(ModDataComponents.TICKET_DATA))
			return false;
		return ticket.getItem() instanceof TicketItem && InventoryUtil.ItemHasTag(ticket, LCTags.Items.TICKETS_MASTER);
	}

	public static long GetTicketID(ItemStack ticket)
	{
		//Get the ticket item
		if(ticket.isEmpty() || !(ticket.getItem() instanceof TicketItem) || !ticket.has(ModDataComponents.TICKET_DATA))
			return Long.MIN_VALUE;
		return ticket.get(ModDataComponents.TICKET_DATA).id();
	}

	public static int GetTicketColor(ItemStack ticket)
	{
		if(ticket.isEmpty() || !(ticket.getItem() instanceof TicketItem) || !ticket.has(ModDataComponents.TICKET_DATA))
			return 0xFFFFFF;
		return ticket.get(ModDataComponents.TICKET_DATA).color();
	}

	public static int GetDefaultTicketColor(long ticketID) {
		if (ticketID == -1)
			return Color.YELLOW.hexColor;
		if(ticketID == -2)
			return Color.BLUE.hexColor;
		return Color.getFromIndex(ticketID).hexColor;
	}

	public static ItemStack CraftTicket(@Nonnull ItemStack master, @Nonnull Item item)
	{
		if(isMasterTicket(master))
			return CreateTicket(item, GetTicketID(master), GetTicketColor(master));
		return ItemStack.EMPTY;
	}

	public static ItemStack CreateTicket(Item item, long ticketID) { return CreateTicket(item, ticketID, TicketItem.GetDefaultTicketColor(ticketID)); }
	public static ItemStack CreateTicket(Item item, long ticketID, int color) { return CreateTicket(item, ticketID, color, 1); }
	public static ItemStack CreateTicket(Item item, long ticketID, int color, int count)
	{
		ItemStack ticket = new ItemStack(item, count);
		ticket.set(ModDataComponents.TICKET_DATA, new TicketData(ticketID, color));
		return ticket;
	}

	public static ItemStack CreateExampleTicket(@Nonnull Item item, @Nonnull Color color)
	{
		ItemStack ticket = new ItemStack(item);
		ticket.set(ModDataComponents.TICKET_DATA, new TicketData(Long.MIN_VALUE,color.hexColor));
		return ticket;
	}

	public static void SetTicketColor(ItemStack ticket, Color color) { SetTicketColor(ticket, color.hexColor); }

	public static void SetTicketColor(ItemStack ticket, int color) {
		if(!ticket.has(ModDataComponents.TICKET_DATA))
			return;
		TicketData oldData = ticket.get(ModDataComponents.TICKET_DATA);
		ticket.set(ModDataComponents.TICKET_DATA, new TicketData(oldData.id(),color));
	}
	
}
