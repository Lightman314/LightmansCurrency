package io.github.lightman314.lightmanscurrency.items;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class TicketItem extends Item{

	public static final ResourceLocation TICKET_TAG = new ResourceLocation(LightmansCurrency.MODID,"ticket");
	public static final ResourceLocation TICKET_MATERIAL_TAG = new ResourceLocation(LightmansCurrency.MODID,"ticket_material");
	public static final TagKey<Item> TICKET_MATERIAL_KEY = TagKey.create(ForgeRegistries.Keys.ITEMS, TICKET_MATERIAL_TAG);
	
	public TicketItem(Properties properties)
	{
		super(properties);
	}
	
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flagIn)
	{
		if(Screen.hasShiftDown())
		{
			UUID ticketID = GetTicketID(stack);
			if(ticketID != null)
				tooltip.add(Component.translatable("tooltip.lightmanscurrency.ticket.id", ticketID));
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
		MutableComponent list = Component.empty();
		boolean firstEntry = true;
		
		try {
			for(Item item : ForgeRegistries.ITEMS.tags().getTag(TICKET_MATERIAL_KEY).stream().collect(Collectors.toList()))
			{
				Component hoverName = new ItemStack(item).getHoverName();
				if(firstEntry)
					list.append(hoverName);
				else
					list.append(Component.translatable("lightmanscurrency.jei.info.ticket_materials.seperator")).append(hoverName);
			}
		} catch(Throwable t) { t.printStackTrace(); }
		
		return list;
	}
	
}
