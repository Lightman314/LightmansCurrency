package io.github.lightman314.lightmanscurrency.common.traders.terminal.filters;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.ITraderSearchFilter;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import javax.annotation.Nonnull;

public class ItemTraderSearchFilter implements ITraderSearchFilter {

	@Override
	public boolean filter(@Nonnull TraderData data, @Nonnull String searchText) {
		
		//Search the items being sold
		if(data instanceof ItemTraderData trader)
		{
			List<ItemTradeData> trades = trader.getTradeData();
			for (ItemTradeData trade : trades) {
				if (trade.isValid()) {
					ItemStack sellItem = trade.getSellItem(0);
					ItemStack sellItem2 = trade.getSellItem(1);
					//Search item name
					if (sellItem.getHoverName().getString().toLowerCase().contains(searchText))
						return true;
					if (sellItem2.getHoverName().getString().toLowerCase().contains(searchText))
						return true;
					//Search custom name
					if (trade.getCustomName(0).toLowerCase().contains(searchText))
						return true;
					if (trade.getCustomName(1).toLowerCase().contains(searchText))
						return true;
					//Search enchantments
					AtomicBoolean foundEnchantment = new AtomicBoolean(false);
					EnchantmentHelper.getEnchantments(sellItem).forEach((enchantment, level) -> {
						if (enchantment.getFullname(level).getString().toLowerCase().contains(searchText))
							foundEnchantment.set(true);
					});
					EnchantmentHelper.getEnchantments(sellItem2).forEach((enchantment, level) -> {
						if (enchantment.getFullname(level).getString().toLowerCase().contains(searchText))
							foundEnchantment.set(true);
					});
					if (foundEnchantment.get())
						return true;

					//Check the barter item if applicable
					if (trade.isBarter()) {
						ItemStack barterItem = trade.getBarterItem(0);
						ItemStack barterItem2 = trade.getBarterItem(1);
						//Search item name
						if (barterItem.getHoverName().getString().toLowerCase().contains(searchText))
							return true;
						if (barterItem2.getHoverName().getString().toLowerCase().contains(searchText))
							return true;
						//Search enchantments
						foundEnchantment.set(false);
						EnchantmentHelper.getEnchantments(barterItem).forEach((enchantment, level) -> {
							if (enchantment.getFullname(level).getString().toLowerCase().contains(searchText))
								foundEnchantment.set(true);
						});
						EnchantmentHelper.getEnchantments(barterItem2).forEach((enchantment, level) -> {
							if (enchantment.getFullname(level).getString().toLowerCase().contains(searchText))
								foundEnchantment.set(true);
						});
						if (foundEnchantment.get())
							return true;
					}
				}
			}
		}
		return false;
	}

}
