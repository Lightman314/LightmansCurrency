package io.github.lightman314.lightmanscurrency.common.items;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.tickets.TicketSaveData;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.Constants;

public class TicketItem extends Item {

	public static final ResourceLocation TICKET_TAG = new ResourceLocation(LightmansCurrency.MODID,"ticket");
	public static final ResourceLocation TICKET_MATERIAL_TAG = new ResourceLocation(LightmansCurrency.MODID,"ticket_material");
	public static final Tags.IOptionalNamedTag<Item> TICKET_MATERIAL = ItemTags.createOptional(TICKET_MATERIAL_TAG);

	public static final long CREATIVE_TICKET_ID = -1;
	public static final int CREATIVE_TICKET_COLOR = 0xFFFF00;

	public TicketItem(Properties properties)
	{
		super(properties);
	}

	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable World level, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn)
	{
		long ticketID = GetTicketID(stack);
		if(ticketID >= -1)
			tooltip.add(EasyText.translatable("tooltip.lightmanscurrency.ticket.id", ticketID));
	}

	public void inventoryTick(@Nonnull ItemStack stack, @Nonnull World level, @Nonnull Entity entity, int slot, boolean selected) {
		GetTicketID(stack);
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
		CompoundNBT ticketTag = ticket.getTag();
		if(ticketTag.contains("TicketID", Constants.NBT.TAG_LONG))
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
		CompoundNBT ticketTag = ticket.getTag();
		if(!ticketTag.contains("TicketColor"))
			return 0xFFFFFF;
		return ticketTag.getInt("TicketColor");
	}

	public static ItemStack CreateMasterTicket(long ticketID) { return CreateMasterTicket(ticketID, Color.getFromIndex(ticketID).hexColor); }

	public static ItemStack CreateMasterTicket(long ticketID, int color)
	{
		ItemStack ticket = new ItemStack(ModItems.TICKET_MASTER.get());
		CompoundNBT tag = ticket.getOrCreateTag();
		tag.putLong("TicketID", ticketID);
		tag.putInt("TicketColor", color);
		return ticket;
	}

	public static ItemStack CreateTicket(ItemStack master)
	{
		if(master.getItem() == ModItems.TICKET_MASTER.get())
			return CreateTicket(GetTicketID(master), GetTicketColor(master));
		return ItemStack.EMPTY;
	}

	public static ItemStack CreateTicket(long ticketID, int color)
	{
		return CreateTicket(ticketID, color,1);
	}

	public static ItemStack CreateTicket(long ticketID, int color, int count)
	{
		ItemStack ticket = new ItemStack(ModItems.TICKET.get(), count);
		CompoundNBT tag = ticket.getOrCreateTag();
		tag.putLong("TicketID", ticketID);
		tag.putInt("TicketColor", color);
		return ticket;
	}

	public static void SetTicketColor(ItemStack ticket, Color color) { SetTicketColor(ticket, color.hexColor); }

	public static void SetTicketColor(ItemStack ticket, int color) {
		CompoundNBT tag = ticket.getOrCreateTag();
		tag.putInt("TicketColor", color);
	}

	public static IFormattableTextComponent getTicketMaterialsList() {
		IFormattableTextComponent list = EasyText.empty();

		try {
			for(Item item : TICKET_MATERIAL.getValues())
			{
				list.append(EasyText.literal("\n")).append(new ItemStack(item).getHoverName());
			}
		} catch(Throwable t) { t.printStackTrace(); }

		return list;
	}

	public void fillItemCategory(@Nonnull ItemGroup tab, @Nonnull NonNullList<ItemStack> itemList) {
		if (this.allowdedIn(tab)) {
			if(this == ModItems.TICKET_MASTER.get())
				itemList.add(TicketItem.CreateMasterTicket(CREATIVE_TICKET_ID, CREATIVE_TICKET_COLOR));
			else
				itemList.add(TicketItem.CreateTicket(CREATIVE_TICKET_ID,CREATIVE_TICKET_COLOR));
		}
	}

}