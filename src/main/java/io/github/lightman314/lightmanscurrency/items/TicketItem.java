package io.github.lightman314.lightmanscurrency.items;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class TicketItem extends Item {

	public static final ResourceLocation TICKET_TAG = new ResourceLocation(LightmansCurrency.MODID,"ticket");
	public static final ResourceLocation TICKET_MATERIAL_TAG = new ResourceLocation(LightmansCurrency.MODID,"ticket_material");
	public static final TagKey<Item> TICKET_MATERIAL_KEY = TagKey.create(ForgeRegistries.Keys.ITEMS, TICKET_MATERIAL_TAG);

	public static final UUID CREATIVE_TICKET_ID = new UUID(0,0);

	public TicketItem(Properties properties)
	{
		super(properties);
	}
	
	@Override
	public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn)
	{
		if(Screen.hasShiftDown())
		{
			UUID ticketID = GetTicketID(stack);
			if(ticketID != null)
				tooltip.add(new TranslatableComponent("tooltip.lightmanscurrency.ticket.id", ticketID));
		}
	}

	@Override
	public void fillItemCategory(@NotNull CreativeModeTab tab, @NotNull NonNullList<ItemStack> itemList) {
		if(this.allowdedIn(tab))
		{
			ItemStack stack = new ItemStack(this);
			stack.getOrCreateTag().putUUID("TicketID", CREATIVE_TICKET_ID);
			itemList.add(stack);
		}
	}
	
	public static boolean isMasterTicket(ItemStack ticket)
	{
		if(ticket.isEmpty() || !ticket.hasTag())
			return false;
		return ticket.getItem() == ModItems.TICKET_MASTER.get();
	}
	
	public static UUID GetTicketID(ItemStack ticket)
	{
		//Get the ticket item
		if(ticket.isEmpty() || !(ticket.getItem() instanceof TicketItem) || !ticket.hasTag())
			return null;
		CompoundTag ticketTag = ticket.getTag();
		if(!ticketTag.contains("TicketID"))
			return null;
		return ticketTag.getUUID("TicketID");
	}
	
	public static ItemStack CreateMasterTicket(UUID ticketID)
	{
		ItemStack ticket = new ItemStack(ModItems.TICKET_MASTER.get());
		if(ticketID != null)
			ticket.getOrCreateTag().putUUID("TicketID", ticketID);
		return ticket;
	}
	
	public static ItemStack CreateTicket(UUID ticketID)
	{
		return CreateTicket(ticketID, 1);
	}
	
	public static ItemStack CreateTicket(UUID ticketID, int count)
	{
		ItemStack ticket = new ItemStack(ModItems.TICKET.get(), count);
		if(ticketID != null)
			ticket.getOrCreateTag().putUUID("TicketID", ticketID);
		return ticket;
	}
	
	public static MutableComponent getTicketMaterialsList() {
		MutableComponent list = new TextComponent("");

		try {
			for(Item item : ForgeRegistries.ITEMS.tags().getTag(TICKET_MATERIAL_KEY).stream().toList())
			{
				list.append(new TextComponent("\n")).append(new ItemStack(item).getHoverName());
			}
		} catch(Throwable t) { t.printStackTrace(); }

		return list;
	}
	
}
