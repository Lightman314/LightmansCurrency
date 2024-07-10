package io.github.lightman314.lightmanscurrency.common.traders.terminal.filters;

import io.github.lightman314.lightmanscurrency.api.traders.terminal.IBasicTraderFilter;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.ITradeSearchFilter;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade_data.SlotMachineTrade;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class SlotMachineSearchFilter implements IBasicTraderFilter {

    @Override
    public boolean filterTrade(@Nonnull TradeData data, @Nonnull String searchText, @Nonnull HolderLookup.Provider lookup) {
        if(data instanceof SlotMachineTrade trade)
        {
            for(SlotMachineEntry entry : trade.trader.getValidEntries())
            {
                for(ItemStack stack : entry.items)
                {
                    if(ITradeSearchFilter.filterItem(stack, searchText, lookup))
                        return true;
                }
                if(entry.isMoney())
                {
                    if(entry.getMoneyValue().getString().toLowerCase().contains(searchText))
                        return true;
                }
            }
        }
        return false;
    }

}
