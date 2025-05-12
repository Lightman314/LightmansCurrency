package io.github.lightman314.lightmanscurrency.common.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class TicketItem extends Item{


	public TicketItem(Properties properties) { super(properties); }

	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn)
	{
		if(isPass(stack))
			tooltip.add(LCText.TOOLTIP_PASS.get());
		long ticketID = GetTicketID(stack);
		if(ticketID >= -2)
			tooltip.add(LCText.TOOLTIP_TICKET_ID.get(ticketID));
		super.appendHoverText(stack,level,tooltip,flagIn);
	}

	public void inventoryTick(@Nonnull ItemStack stack, @Nonnull Level level, @Nonnull Entity entity, int slot, boolean selected) {
		GetTicketID(stack);
	}

	public static boolean isTicket(ItemStack ticket)
	{
		if(ticket.isEmpty() ||!ticket.hasTag())
			return false;
		return ticket.getItem() instanceof TicketItem && InventoryUtil.ItemHasTag(ticket, LCTags.Items.TICKETS_TICKET);
	}

	public static boolean isPass(ItemStack ticket)
	{
		if(ticket.isEmpty() || !ticket.hasTag())
			return false;
		return ticket.getItem() instanceof TicketItem && InventoryUtil.ItemHasTag(ticket, LCTags.Items.TICKETS_PASS);
	}

	public static boolean isTicketOrPass(ItemStack ticket)  { return isTicket(ticket) || isPass(ticket); }

	public static boolean isMasterTicket(ItemStack ticket)
	{
		if(ticket.isEmpty() || !ticket.hasTag())
			return false;
		return ticket.getItem() instanceof TicketItem && InventoryUtil.ItemHasTag(ticket, LCTags.Items.TICKETS_MASTER);
	}

	public static long GetTicketID(ItemStack ticket)
	{
		//Get the ticket item
		if(ticket.isEmpty() || !(ticket.getItem() instanceof TicketItem) || !ticket.hasTag())
			return Long.MIN_VALUE;
		CompoundTag ticketTag = ticket.getTag();
		if(ticketTag.contains("TicketID", Tag.TAG_LONG))
			return ticketTag.getLong("TicketID");
		return Long.MIN_VALUE;
	}

	public static int GetTicketColor(ItemStack ticket)
	{
		if(ticket.isEmpty() || !(ticket.getItem() instanceof TicketItem) || !ticket.hasTag())
			return 0xFFFFFF;
		CompoundTag ticketTag = ticket.getTag();
		if(!ticketTag.contains("TicketColor"))
			return 0xFFFFFF;
		return ticketTag.getInt("TicketColor");
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
		CompoundTag tag = ticket.getOrCreateTag();
		tag.putLong("TicketID", ticketID);
		tag.putInt("TicketColor", color);
		return ticket;
	}

	public static ItemStack CreateExampleTicket(@Nonnull Item item, @Nonnull Color color)
	{
		ItemStack ticket = new ItemStack(item);
		CompoundTag tag = ticket.getOrCreateTag();
		tag.putInt("TicketColor", color.hexColor);
		return ticket;
	}

	public static void SetTicketColor(ItemStack ticket, Color color) { SetTicketColor(ticket, color.hexColor); }

	public static void SetTicketColor(ItemStack ticket, int color) {
		CompoundTag tag = ticket.getOrCreateTag();
		tag.putInt("TicketColor", color);
	}
	
}
