package io.github.lightman314.lightmanscurrency.trader.tradedata.restrictions;

import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.items.TicketItem;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;

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
			sellItem.setDisplayName(new StringTextComponent(trade.getCustomName()));
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
	public int getSaleStock(ItemStack sellItem, IInventory traderStorage)
	{
		if(sellItem.getItem() == ModItems.TICKET)
			return InventoryUtil.GetItemTagCount(traderStorage, TicketItem.TICKET_MATERIAL_TAG, ModItems.TICKET_MASTER) / sellItem.getCount();
		return super.getSaleStock(sellItem, traderStorage);
	}
	
	@Override
	public void removeItemsFromStorage(ItemStack sellItem, IInventory traderStorage)
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
	
}
