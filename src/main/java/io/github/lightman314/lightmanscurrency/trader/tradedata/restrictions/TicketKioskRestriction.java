package io.github.lightman314.lightmanscurrency.trader.tradedata.restrictions;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.items.TicketItem;
import io.github.lightman314.lightmanscurrency.menus.slots.TicketSlot;
import io.github.lightman314.lightmanscurrency.trader.common.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
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
	public ItemStack modifySellItem(ItemStack sellItem, String customName, ItemTradeData trade)
	{
		if(sellItem.getItem() instanceof TicketItem && !customName.isBlank())
			sellItem.setHoverName(new TextComponent(customName));
		return sellItem;
	}
	
	@Override
	public boolean allowSellItem(ItemStack itemStack)
	{
		if(TicketItem.isMasterTicket(itemStack))
			return true;
		return InventoryUtil.ItemHasTag(itemStack, TicketItem.TICKET_MATERIAL_TAG) && itemStack.getItem() != ModItems.TICKET;
	}
	
	@Override
	public ItemStack filterSellItem(ItemStack itemStack)
	{
		if(TicketItem.isMasterTicket(itemStack))
			return TicketItem.CreateTicket(TicketItem.GetTicketID(itemStack), 1);
		else if(InventoryUtil.ItemHasTag(itemStack, TicketItem.TICKET_MATERIAL_TAG) && itemStack.getItem() != ModItems.TICKET)
			return itemStack;
		else
			return ItemStack.EMPTY;
	}
	
	@Override
	public boolean allowItemSelectItem(ItemStack itemStack)
	{
		Item item = itemStack.getItem();
		return InventoryUtil.ItemHasTag(itemStack, TicketItem.TICKET_MATERIAL_TAG) && item != ModItems.TICKET && item != ModItems.TICKET_MASTER;
	}
	
	@Override
	public int getSaleStock(ItemStack sellItem, TraderItemStorage traderStorage)
	{
		if(sellItem.getItem() == ModItems.TICKET)
			return traderStorage.getItemTagCount(TicketItem.TICKET_MATERIAL_TAG, ModItems.TICKET_MASTER) / sellItem.getCount();
		return super.getSaleStock(sellItem, traderStorage);
	}
	
	@Override
	public void removeItemsFromStorage(ItemStack sellItem, TraderItemStorage traderStorage)
	{
		if(sellItem.getItem() == ModItems.TICKET)
		{
			int amountToRemove = sellItem.getCount();
			amountToRemove -= traderStorage.removeItem(sellItem).getCount();
			if(amountToRemove > 0)
			{
				traderStorage.removeItemTagCount(TicketItem.TICKET_MATERIAL_TAG, amountToRemove, ModItems.TICKET_MASTER);
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
