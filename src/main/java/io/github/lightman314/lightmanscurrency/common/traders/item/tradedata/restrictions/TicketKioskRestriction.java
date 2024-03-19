package io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ticket.TicketData;
import io.github.lightman314.lightmanscurrency.common.traders.item.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.menus.slots.ticket.TicketSlot;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class TicketKioskRestriction extends ItemTradeRestriction{

	public static TicketKioskRestriction INSTANCE = new TicketKioskRestriction();
	
	private TicketKioskRestriction() {}
	
	@Override
	public ItemStack modifySellItem(ItemStack sellItem, String customName, ItemTradeData trade)
	{
		if(TicketItem.isTicket(sellItem) && !customName.isBlank())
			sellItem.setHoverName(Component.literal(customName));
		return sellItem;
	}
	
	@Override
	public boolean allowSellItem(ItemStack itemStack)
	{
		if(TicketItem.isMasterTicket(itemStack))
			return true;
		return InventoryUtil.ItemHasTag(itemStack, LCTags.Items.TICKET_MATERIAL) && !TicketItem.isTicketOrPass(itemStack);
	}
	
	@Override
	public ItemStack filterSellItem(ItemStack itemStack)
	{
		if(TicketItem.isMasterTicket(itemStack))
		{
			TicketData data = TicketData.getForMaster(itemStack);
			if(data != null)
				return TicketItem.CraftTicket(itemStack, data.ticket.asItem());
		}
		else if(InventoryUtil.ItemHasTag(itemStack, LCTags.Items.TICKET_MATERIAL) && !TicketItem.isTicketOrPass(itemStack))
			return itemStack;
		return ItemStack.EMPTY;
	}
	
	@Override
	public boolean allowItemSelectItem(ItemStack itemStack)
	{
		return InventoryUtil.ItemHasTag(itemStack, LCTags.Items.TICKET_MATERIAL) && !InventoryUtil.ItemHasTag(itemStack, LCTags.Items.TICKETS);
	}
	
	@Override
	public boolean allowExtraItemInStorage(ItemStack itemStack) {
		return InventoryUtil.ItemHasTag(itemStack, LCTags.Items.TICKET_MATERIAL);
	}

	@Override
	public int getSaleStock(TraderItemStorage traderStorage, ItemTradeData trade) {
		List<Pair<TicketData,Integer>> countByCategory = new ArrayList<>();
		boolean foundTicket = false;
		int minStock = Integer.MAX_VALUE;
		for(ItemStack sellItem : Lists.newArrayList(trade.getSellItem(0), trade.getSellItem(1))) {
			//Always add item to the ticket count, even if it's not a ticket, as the non-ticket sell item will still subtract from the available printing materials.
			int count = sellItem.getCount();
			if(TicketItem.isTicket(sellItem))
			{
				TicketData data = TicketData.getForTicket(sellItem);
				if(data != null)
				{
					foundTicket = true;
					addToList(data, count, countByCategory);
					continue;
				}
			}
			TicketData data = TicketData.getForMaterial(sellItem);
			if(data != null)
				addToList(data, count, countByCategory);
			minStock = Math.min(this.getItemStock(sellItem, traderStorage), minStock);
		}
		if(foundTicket && !countByCategory.isEmpty())
			minStock = Math.min(this.getTicketStock(countByCategory, traderStorage), minStock);
		return minStock;
	}

	private void addToList(@Nonnull TicketData data, int count, @Nonnull List<Pair<TicketData,Integer>> countByCategory)
	{
		for(int i = 0; i < countByCategory.size(); ++i)
		{
			Pair<TicketData,Integer> pair = countByCategory.get(i);
			if(pair.getFirst() == data)
			{
				countByCategory.set(i, Pair.of(data,pair.getSecond() + count));
				return;
			}
		}
		countByCategory.add(Pair.of(data, count));
	}

	protected final int getTicketStock(List<Pair<TicketData,Integer>> data, @Nonnull TraderItemStorage traderStorage)
	{
		int minStock = Integer.MAX_VALUE;
		for(var pair : data)
			minStock = Math.min(traderStorage.getItemTagCount(pair.getFirst().material, pair.getFirst().masterTicket) / pair.getSecond(),minStock);
		return minStock;
	}
	
	@Override
	public void removeItemsFromStorage(TraderItemStorage traderStorage, List<ItemStack> soldItems)
	{
		//Sort out the tickets, and remove "normal" items from storage.
		List<ItemStack> tickets = new ArrayList<>();
		List<ItemStack> ignoreIfPossible = new ArrayList<>();
		for(ItemStack sellItem : soldItems)
		{
			if(TicketItem.isTicket(sellItem))
				tickets.add(sellItem);
			else
			{
				this.removeFromStorage(sellItem, traderStorage);
				ignoreIfPossible.add(sellItem);
			}
		}
		//Attempt to remove the tickets from storage "normally".
		//Keep track of how many need to be printed.
		int printCount = 0;
		for(ItemStack ticketStack : tickets)
		{
			printCount += ticketStack.getCount() - traderStorage.removeItem(ticketStack).getCount();
			if(printCount > 0)
			{
				TicketData data = TicketData.getForTicket(ticketStack);
				if(data == null)
					LightmansCurrency.LogWarning("Missing Ticket Kiosk Data for " + ticketStack.getHoverName().getString());
				else
				{
					//Remove materials for this ticket
					traderStorage.removeItemTagCount(data.material, printCount, ignoreIfPossible, data.masterTicket);
				}
			}
		}
	}

	@Override
	public boolean alwaysEnforceNBT(int tradeSlot) { return tradeSlot < 2; }

	@Override
	@OnlyIn(Dist.CLIENT)
	public Pair<ResourceLocation,ResourceLocation> getEmptySlotBG()
	{
		return Pair.of(InventoryMenu.BLOCK_ATLAS, TicketSlot.EMPTY_TICKET_SLOT);
	}
	
}
