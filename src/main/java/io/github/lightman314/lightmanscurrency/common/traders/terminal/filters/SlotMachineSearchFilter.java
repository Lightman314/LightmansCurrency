package io.github.lightman314.lightmanscurrency.common.traders.terminal.filters;

import io.github.lightman314.lightmanscurrency.api.traders.terminal.IBasicTraderFilter;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.PendingSearch;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade_data.SlotMachineTrade;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class SlotMachineSearchFilter implements IBasicTraderFilter {

    @Override
    public void filterTrade(@Nonnull TradeData data, @Nonnull PendingSearch search, @Nonnull HolderLookup.Provider lookup) {
        if(data instanceof SlotMachineTrade trade)
        {
            List<ItemStack> items = new ArrayList<>();
            for(SlotMachineEntry entry : trade.trader.getValidEntries())
            {
                if(entry.isValid())
                    items.addAll(entry.items);
            }
            search.processFilter(ItemTraderSearchFilter.ITEM,ItemTraderSearchFilter.filterItems(items,lookup));
            search.processFilter(BasicSearchFilter.TOOLTIP,ItemTraderSearchFilter.filterItemTooltips(items,lookup));
        }
    }

}
