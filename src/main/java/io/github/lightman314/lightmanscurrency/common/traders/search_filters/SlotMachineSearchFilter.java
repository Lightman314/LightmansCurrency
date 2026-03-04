package io.github.lightman314.lightmanscurrency.common.traders.search_filters;

import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.IBasicTraderFilter;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.PendingSearch;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.builtin.BasicSearchFilter;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.nodes.SlotMachineNode;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade.SlotMachineDummyTrade;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SlotMachineSearchFilter implements IBasicTraderFilter {

    @Override
    public void filterTrade(TradeData data, PendingSearch search, HolderLookup.Provider lookup) {
        if(data instanceof SlotMachineDummyTrade trade)
        {

            TraderData trader = trade.getTrader();
            if(trader != null)
            {
                SlotMachineNode node = trader.getNode(SlotMachineNode.TYPE);
                if(node != null)
                {
                    List<ItemStack> items = new ArrayList<>();
                    for(SlotMachineEntry entry : node.getValidEntries())
                    {
                        if(entry.isValid())
                            items.addAll(entry.items);
                    }
                    search.processFilter(ItemTraderSearchFilter.ITEM,ItemTraderSearchFilter.filterItems(items,lookup));
                    search.processFilter(BasicSearchFilter.TOOLTIP,ItemTraderSearchFilter.filterItemTooltips(items,lookup));
                }
            }
        }
    }

}
