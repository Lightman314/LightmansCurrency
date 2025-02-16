package io.github.lightman314.lightmanscurrency.common.traders.terminal.filters;

import io.github.lightman314.lightmanscurrency.api.traders.terminal.IBasicTraderFilter;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.ITradeSearchFilter;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.PendingSearch;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemTraderSearchFilter implements IBasicTraderFilter {

	public static final String ITEM = "item";

	@Override
	public void filterTrade(@Nonnull TradeData data, @Nonnull PendingSearch search) {
		if(data instanceof ItemTradeData trade)
		{
			if (trade.isValid()) {
				List<ItemWithContext> itemList = new ArrayList<>();
				itemList.add(new ItemWithContext(trade.getSellItem(0),trade.getCustomName(0)));
				itemList.add(new ItemWithContext(trade.getSellItem(1),trade.getCustomName(1)));
				if(trade.isBarter())
				{
					itemList.add(new ItemWithContext(trade.getBarterItem(0),null));
					itemList.add(new ItemWithContext(trade.getBarterItem(1),null));
				}
				search.processFilter(ITEM,filterItemsWithContext(itemList));
			}
		}
	}

	public static Predicate<String> filterItems(List<ItemStack> items)
	{
		List<ItemWithContext> list = new ArrayList<>();
		items.forEach(i -> list.add(new ItemWithContext(i,null)));
		return filterItemsWithContext(list);
	}
	public static Predicate<String> filterItemsWithContext(List<ItemWithContext> items)
	{
		return input -> {
			for(ItemWithContext item : items)
			{
				if(item.isEmpty())
					continue;
				if(ITradeSearchFilter.filterItem(item.item,input))
					return true;
				if(item.customName != null && !item.customName.isBlank() && item.customName.toLowerCase().contains(input))
					return true;
			}
			return false;
		};
	}

	public record ItemWithContext(ItemStack item, @Nullable String customName) { public boolean isEmpty() { return this.item.isEmpty(); }}

}