package io.github.lightman314.lightmanscurrency.trader.tradedata.restrictions;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.items.TicketItem;
import io.github.lightman314.lightmanscurrency.menus.slots.TicketSlot;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TicketKioskRestriction extends ItemTradeRestriction{

	public TicketKioskRestriction() {} 
	
	public TicketKioskRestriction(String classicType)
	{
		super(classicType);
	}
	
	@Override
	public ItemStack modifySellItem(ItemStack sellItem, ItemTradeData trade)
	{
		if(sellItem.getItem() instanceof TicketItem && trade.hasCustomName())
			sellItem.setHoverName(new TextComponent(trade.getCustomName()));
		return sellItem;
	}
	
	@Override
	public boolean allowSellItem(ItemStack itemStack)
	{
		if(TicketItem.isMasterTicket(itemStack))
			return true;
		return itemStack.getItem().getTags().contains(TicketItem.TICKET_MATERIAL_TAG) && itemStack.getItem() != ModItems.TICKET;
	}
	
	@Override
	public ItemStack filterSellItem(ItemStack itemStack)
	{
		if(TicketItem.isMasterTicket(itemStack))
			return TicketItem.CreateTicket(TicketItem.GetTicketID(itemStack), 1);
		else if(itemStack.getItem().getTags().contains(TicketItem.TICKET_MATERIAL_TAG) && itemStack.getItem() != ModItems.TICKET)
			return itemStack;
		else
			return ItemStack.EMPTY;
	}
	
	@Override
	public int getSaleStock(ItemStack sellItem, Container traderStorage)
	{
		if(sellItem.getItem() == ModItems.TICKET)
			return InventoryUtil.GetItemTagCount(traderStorage, TicketItem.TICKET_MATERIAL_TAG, ModItems.TICKET_MASTER) / sellItem.getCount();
		return super.getSaleStock(sellItem, traderStorage);
	}
	
	@Override
	public void removeItemsFromStorage(ItemStack sellItem, Container traderStorage)
	{
		if(sellItem.getItem() == ModItems.TICKET)
		{
			if(!InventoryUtil.RemoveItemCount(traderStorage, sellItem))
			{
				InventoryUtil.RemoveItemTagCount(traderStorage, TicketItem.TICKET_MATERIAL_TAG, sellItem.getCount(), ModItems.TICKET_MASTER);
			}
		}
		else
			super.removeItemsFromStorage(sellItem, traderStorage);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public Pair<ResourceLocation,ResourceLocation> getEmptySlotBG()
	{
		return Pair.of(InventoryMenu.BLOCK_ATLAS, TicketSlot.EMPTY_TICKET_SLOT);
	}
	
}
