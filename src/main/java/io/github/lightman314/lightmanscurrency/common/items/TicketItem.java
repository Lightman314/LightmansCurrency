package io.github.lightman314.lightmanscurrency.common.items;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.tickets.TicketSaveData;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class TicketItem extends Item{

	public static final ResourceLocation TICKET_TAG = new ResourceLocation(LightmansCurrency.MODID,"ticket");
	public static final ResourceLocation TICKET_MATERIAL_TAG = new ResourceLocation(LightmansCurrency.MODID,"ticket_material");
	public static final TagKey<Item> TICKET_MATERIAL_KEY = TagKey.create(ForgeRegistries.Keys.ITEMS, TICKET_MATERIAL_TAG);

	public static final long CREATIVE_TICKET_ID = -1;
	public static final int CREATIVE_TICKET_COLOR = 0xFFFF00;

	public TicketItem(Properties properties)
	{
		super(properties);
	}
	
	@Override
	public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn)
	{
		if(isPass(stack))
			tooltip.add(EasyText.translatable("tooltip.lightmanscurrency.ticket.pass"));
		long ticketID = GetTicketID(stack);
		if(ticketID >= -1)
			tooltip.add(Component.translatable("tooltip.lightmanscurrency.ticket.id", ticketID));
	}

	public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
		GetTicketID(stack);
	}

	public static boolean isTicket(ItemStack ticket)
	{
		if(ticket.isEmpty() || !ticket.hasTag())
			return false;
		return ticket.getItem() == ModItems.TICKET.get();
	}

	public static boolean isPass(ItemStack ticket)
	{
		if(ticket.isEmpty() || !ticket.hasTag())
			return false;
		return ticket.getItem() == ModItems.TICKET_PASS.get();
	}

	public static boolean isTicketOrPass(ItemStack ticket)
	{
		if(ticket.isEmpty() || !ticket.hasTag())
			return false;
		return ticket.getItem() == ModItems.TICKET.get() || ticket.getItem() == ModItems.TICKET_PASS.get();
	}

	public static boolean isMasterTicket(ItemStack ticket)
	{
		if(ticket.isEmpty() || !ticket.hasTag())
			return false;
		return ticket.getItem() == ModItems.TICKET_MASTER.get();
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
		if (ticketID == CREATIVE_TICKET_ID)
			return Color.YELLOW.hexColor;
		return Color.getFromIndex(ticketID).hexColor;
	}

	public static ItemStack CreateMasterTicket(long ticketID) { return CreateMasterTicket(ticketID, Color.getFromIndex(ticketID).hexColor); }
	
	public static ItemStack CreateMasterTicket(long ticketID, int color) { return CreateTicketInternal(ModItems.TICKET_MASTER.get(), ticketID, color, 1); }

	public static ItemStack CreateTicket(ItemStack master)
	{
		if(master.getItem() == ModItems.TICKET_MASTER.get())
			return CreateTicket(GetTicketID(master), GetTicketColor(master));
		return ItemStack.EMPTY;
	}

	public static ItemStack CreatePass(long ticketID, int color) { return CreatePass(ticketID, color,1); }
	public static ItemStack CreatePass(long ticketID, int color, int count) { return CreateTicketInternal(ModItems.TICKET_PASS.get(), ticketID, color,count); }

	public static ItemStack CreateTicket(long ticketID, int color) { return CreateTicket(ticketID, color,1); }
	
	public static ItemStack CreateTicket(long ticketID, int color, int count) { return CreateTicketInternal(ModItems.TICKET.get(), ticketID, color, count); }

	private static ItemStack CreateTicketInternal(Item item, long ticketID, int color, int count)
	{
		ItemStack ticket = new ItemStack(item, count);
		CompoundTag tag = ticket.getOrCreateTag();
		tag.putLong("TicketID", ticketID);
		tag.putInt("TicketColor", color);
		return ticket;
	}

	public static void SetTicketColor(ItemStack ticket, Color color) { SetTicketColor(ticket, color.hexColor); }

	public static void SetTicketColor(ItemStack ticket, int color) {
		CompoundTag tag = ticket.getOrCreateTag();
		tag.putInt("TicketColor", color);
	}

	public static MutableComponent getTicketMaterialsList() {
		MutableComponent list = Component.empty();
		
		try {
			for(Item item : ForgeRegistries.ITEMS.tags().getTag(TICKET_MATERIAL_KEY).stream().toList())
			{
				list.append(Component.literal("\n")).append(new ItemStack(item).getHoverName());
			}
		} catch(Throwable t) { t.printStackTrace(); }
		
		return list;
	}
	
}
