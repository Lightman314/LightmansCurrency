package io.github.lightman314.lightmanscurrency.common.traders.item.ticket;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.common.crafting.TicketStationRecipe;
import io.github.lightman314.lightmanscurrency.common.menus.slots.ticket.TicketModifierSlot;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.util.ItemRequirement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class TicketKioskRestriction extends ItemTradeRestriction {

	public static TicketKioskRestriction REGISTERED_INSTANCE = new TicketKioskRestriction(new TicketItemTrade(false));

	private final TicketItemTrade trade;
	public TicketKioskRestriction(TicketItemTrade trade) {
		this.trade = trade;
	}

	@Override
	public ResourceLocation getType() { return getId(REGISTERED_INSTANCE); }

	@Override
	public ItemStack modifySellItem(ItemStack sellItem, String customName, ItemTradeData trade, int index)
	{
		if(trade != this.trade)
			return sellItem;
		TicketSaleData data = this.trade.getTicketData(index);
		if(data == null)
			return sellItem;
		return data.getCraftingResult(true);
	}

	@Override
	public boolean displayCustomName(ItemStack sellItem, ItemTradeData trade, int index) {
		if(trade != this.trade)
			return true;
		TicketSaleData data = this.trade.getTicketData(index);
		return data != null && data.isRecipeMode();
	}

	@Override
	public boolean allowSellItem(ItemStack stack)
	{
		if(TicketItem.isMasterTicket(stack))
			return true;
		return stack.is(LCTags.Items.TICKET_MATERIAL) && !TicketItem.isTicketOrPass(stack);
	}
	
	@Override
	public ItemStack filterSellItem(ItemStack stack)
	{
		if(TicketItem.isMasterTicket(stack))
			return stack;
		else if(stack.is(LCTags.Items.TICKET_MATERIAL) && !TicketItem.isTicketOrPass(stack))
			return stack;
		return ItemStack.EMPTY;
	}
	
	@Override
	public boolean allowItemSelectItem(ItemStack stack)
	{
		return stack.is(LCTags.Items.TICKET_MATERIAL) && !stack.is(LCTags.Items.TICKETS);
	}

    @Override
    public boolean allowFilters() { return false; }

    @Override
	public boolean allowExtraItemInStorage(ItemStack stack) { return stack.is(LCTags.Items.TICKET_MATERIAL); }

	@Override
	public int getSaleStock(TraderItemStorage traderStorage, ItemTradeData trade) {
		if(trade != this.trade)
        {
            LightmansCurrency.LogWarning("Attempted to get stock for a ticket trade that didn't match its restriction!");
            return 0;
        }
		List<Pair<ResourceLocation,Integer>> countByRecipe = new ArrayList<>();
		int minStock = Integer.MAX_VALUE;
		List<ItemRequirement> requirements = new ArrayList<>();
		for(int i = 0; i < 2; ++i)
		{
			ItemStack sellItem = trade.getActualItem(i);
			TicketSaleData data = this.trade.getTicketData(i);
			if(data.isRecipeMode())
			{
				TicketStationRecipe recipe = data.tryGetRecipe();
				if(recipe == null)
					return 0;
				addToList(recipe.getKioskStorageRequirement(sellItem),requirements);
			}
			else
			{
				addToList(ItemRequirement.of(sellItem),requirements);
			}
		}
		if(requirements.isEmpty())
        {
            return 0;
        }
		for(ItemRequirement entry : requirements)
		{
			if(entry.getCount() > 0)
				minStock = traderStorage.getItemCount(entry) / entry.getCount();
		}
		return minStock;
	}

	private void addToList(ItemRequirement requirement, List<ItemRequirement> list)
	{
		for(ItemRequirement entry : list)
		{
			if(entry.tryMerge(requirement))
				return;
		}
		list.add(requirement);
	}

    @Override
    public List<ItemStack> getRandomSellItems(TraderData trader, ItemTradeData trade) {
        if(trade != this.trade)
            return null;
        List<ItemStack> results = new ArrayList<>();
        List<RequirementWithContext> normalSellItems = new ArrayList<>();
        for(int i = 0; i < 2; ++i)
        {
            TicketSaleData data = this.trade.getTicketData(i);
            if(data.isRecipeMode())
                results.add(data.getCraftingResult(true));
            else if(!this.trade.getActualItem(i).isEmpty())
                normalSellItems.add(new RequirementWithContext(trade,i));
        }
        if(!normalSellItems.isEmpty())
        {
            //Get normal results if a non-ticket trade is also present
            List<ItemStack> normal = this.getRandomSellItems(trader,normalSellItems);
            if(normal == null)
                return null; //Failed to collect the "normal" items from storage
            results.addAll(normal);
        }
        return results;
    }

	@Override
	public void removeItemsFromStorage(TraderItemStorage traderStorage, List<ItemStack> soldItems)
	{
		//Sort out the tickets, and remove "normal" items from storage.
		List<ItemRequirement> ingredients = new ArrayList<>();
		List<ItemStack> ignoreIfPossible = new ArrayList<>();
		//Ignore the sold items input list, as NBT is always enforced here
		for(int i = 0; i < 2; ++i)
		{
			ItemStack sellItem = this.trade.getSellItem(i);
            ItemStack actualItem = this.trade.getActualItem(i);
			TicketSaleData data = this.trade.getTicketData(i);
			if(data.isRecipeMode())
			{
				TicketStationRecipe recipe = data.tryGetRecipe();
				if(recipe == null)
					continue;
				addToList(recipe.getKioskStorageRequirement(sellItem),ingredients);
			}
			else
			{
				this.removeFromStorage(sellItem,traderStorage);
				ignoreIfPossible.add(sellItem);
                ignoreIfPossible.add(actualItem);
			}
		}
		//Attempt to remove the tickets from storage "normally".
		//Keep track of how many need to be printed.
		List<Item> masterTickets = new ArrayList<>();
        BuiltInRegistries.ITEM.getTagOrEmpty(LCTags.Items.TICKETS_MASTER).forEach(h -> masterTickets.add(h.value()));
		for(ItemRequirement entry : ingredients)
		{
			traderStorage.removeItemCount(entry,entry.getCount(),ignoreIfPossible,s -> masterTickets.stream().anyMatch(e -> e == s.getItem()));
		}
	}

	@Override
	public boolean alwaysEnforceNBT(int tradeSlot) { return tradeSlot < 2; }

	@Override
	@OnlyIn(Dist.CLIENT)
	public Pair<ResourceLocation,ResourceLocation> getEmptySlotBG()
	{
		return Pair.of(InventoryMenu.BLOCK_ATLAS,TicketModifierSlot.EMPTY_TICKET_SLOT);
	}
	
}
