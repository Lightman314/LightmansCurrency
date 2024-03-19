package io.github.lightman314.lightmanscurrency.common.items;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.tickets.TicketSaveData;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class TicketItem extends Item{

	private final long creativeID;
	private final int creativeColor;

	public TicketItem(Properties properties, long creativeID, Color creativeColor) { this(properties,creativeID, creativeColor.hexColor); }
	public TicketItem(Properties properties, long creativeID, int creativeColor) {
		super(properties);
		this.creativeID = creativeID;
		this.creativeColor = creativeColor;
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn)
	{
		if(isPass(stack))
			tooltip.add(EasyText.translatable("tooltip.lightmanscurrency.ticket.pass"));
		long ticketID = GetTicketID(stack);
		if(ticketID >= -2)
			tooltip.add(EasyText.translatable("tooltip.lightmanscurrency.ticket.id", ticketID));
	}

	public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
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
		else if(ticketTag.contains("TicketID"))
		{
			UUID oldID = ticketTag.getUUID("TicketID");
			long newID = TicketSaveData.getConvertedID(oldID);
			ticketTag.putLong("TicketID", newID);
			ticketTag.putInt("TicketColor", Color.getFromIndex(newID).hexColor);
			return newID;
		}
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
			return CreateTicket(item, GetTicketID(master), GetTicketColor(master), 1);
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

	@Override
	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list) {
		if(this.allowedIn(tab))
			list.add(CreateTicket(this, this.creativeID, this.creativeColor));
	}
}