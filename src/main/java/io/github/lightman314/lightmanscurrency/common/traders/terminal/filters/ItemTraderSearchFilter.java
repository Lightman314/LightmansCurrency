package io.github.lightman314.lightmanscurrency.common.traders.terminal.filters;

import io.github.lightman314.lightmanscurrency.api.traders.terminal.IBasicTraderFilter;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.ITradeSearchFilter;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemTraderSearchFilter implements IBasicTraderFilter {

	@Override
	public boolean filterTrade(@Nonnull TradeData data, @Nonnull String searchText, @Nonnull HolderLookup.Provider lookup) {
		if(data instanceof ItemTradeData trade)
		{
			if (trade.isValid()) {
				ItemStack sellItem = trade.getSellItem(0);
				ItemStack sellItem2 = trade.getSellItem(1);
				//Search item
				if (ITradeSearchFilter.filterItem(sellItem,searchText,lookup))
					return true;
				if (ITradeSearchFilter.filterItem(sellItem2,searchText,lookup))
					return true;
				//Search custom name
				if (!sellItem.isEmpty() && trade.getCustomName(0).toLowerCase().contains(searchText))
					return true;
				if (!sellItem2.isEmpty() && trade.getCustomName(1).toLowerCase().contains(searchText))
					return true;

				//Check the barter item if applicable
				if (trade.isBarter()) {
					ItemStack barterItem = trade.getBarterItem(0);
					ItemStack barterItem2 = trade.getBarterItem(1);
					//Search item name
					if (ITradeSearchFilter.filterItem(barterItem,searchText,lookup))
						return true;
					if (ITradeSearchFilter.filterItem(barterItem2,searchText,lookup))
						return true;
				}
			}
		}
		return false;
	}

}
