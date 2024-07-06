package io.github.lightman314.lightmanscurrency.common.traders.terminal.filters;

import java.util.List;
import java.util.Map;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.ITradeSearchFilter;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.ITraderSearchFilter;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import javax.annotation.Nonnull;

public class ItemTraderSearchFilter implements ITraderSearchFilter, ITradeSearchFilter {

	@Override
	public boolean filter(@Nonnull TraderData data, @Nonnull String searchText) {

		//Search the items being sold
		if(data instanceof ItemTraderData trader)
		{
			List<ItemTradeData> trades = trader.getTradeData();
			for (ItemTradeData trade : trades) {
				if(this.filterTrade(trade,searchText))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean filterTrade(@Nonnull TradeData data, @Nonnull String searchText) {
		if(data instanceof ItemTradeData trade)
		{
			if (trade.isValid()) {
				ItemStack sellItem = trade.getSellItem(0);
				ItemStack sellItem2 = trade.getSellItem(1);
				//Search item name
				if (!sellItem.isEmpty() && sellItem.getHoverName().getString().toLowerCase().contains(searchText))
					return true;
				if (!sellItem2.isEmpty() && sellItem2.getHoverName().getString().toLowerCase().contains(searchText))
					return true;
				//Search custom name
				if (!sellItem.isEmpty() && trade.getCustomName(0).toLowerCase().contains(searchText))
					return true;
				if (!sellItem2.isEmpty() && trade.getCustomName(1).toLowerCase().contains(searchText))
					return true;
				//Search enchantments
				if(searchEnchantments(sellItem,searchText))
					return true;
				if(searchEnchantments(sellItem2,searchText))
					return true;

				//Check the barter item if applicable
				if (trade.isBarter()) {
					ItemStack barterItem = trade.getBarterItem(0);
					ItemStack barterItem2 = trade.getBarterItem(1);
					//Search item name
					if (!barterItem.isEmpty() && barterItem.getHoverName().getString().toLowerCase().contains(searchText))
						return true;
					if (!barterItem2.isEmpty() && barterItem2.getHoverName().getString().toLowerCase().contains(searchText))
						return true;
					//Search enchantments
					if(searchEnchantments(barterItem,searchText))
						return true;
					if(searchEnchantments(barterItem2,searchText))
						return true;
				}
			}
		}
		return false;
	}

	private static boolean searchEnchantments(@Nonnull ItemStack stack, @Nonnull String searchText)
	{
		Map<Enchantment,Integer> enchantments = stack.getAllEnchantments();
		for(var enchantment : enchantments.entrySet())
		{
			if(enchantment.getKey().getFullname(enchantment.getValue()).getString().toLowerCase().contains(searchText))
				return true;
		}
		return false;
	}

}