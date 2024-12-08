package io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade_data.client;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.AlertData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade_data.SlotMachineTrade;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.util.ListUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SlotMachineTradeButtonRenderer extends TradeRenderManager<SlotMachineTrade> {

    public SlotMachineTradeButtonRenderer(SlotMachineTrade trade) { super(trade); }

    @Override
    public int tradeButtonWidth(TradeContext context) { return 128; }

    @Override
    public Optional<ScreenPosition> arrowPosition(TradeContext context) { return ScreenPosition.ofOptional(36, 1); }

    @Override
    public DisplayData inputDisplayArea(TradeContext context) { return new DisplayData(1, 1, 34, 16); }

    @Override
    public List<DisplayEntry> getInputDisplays(TradeContext context) { return this.lazyPriceDisplayList(context); }

    @Override
    public DisplayData outputDisplayArea(TradeContext context) { return new DisplayData(59, 1, 68, 16); }

    @Nullable
    private SlotMachineEntry getTimedEntry()
    {
        return ListUtil.randomItemFromList(this.trade.trader.getValidEntries(),(Supplier<SlotMachineEntry>)() -> null);
    }

    @Override
    public List<DisplayEntry> getOutputDisplays(TradeContext context) {
        SlotMachineEntry entry = this.getTimedEntry();
        if(entry == null)
            return new ArrayList<>();
        List<DisplayEntry> entries = new ArrayList<>();
        String odds = this.trade.trader.getOdds(entry.getWeight());
        for(ItemStack item : entry.items)
            entries.add(DisplayEntry.of(item, item.getCount(), this.tweakTooltip(entry.getWeight(), odds)));
        return entries;
    }

    private Consumer<List<Component>> tweakTooltip(int weight, String odds)
    {
        return tooltips -> {
            tooltips.addFirst(LCText.TOOLTIP_SLOT_MACHINE_WEIGHT.get(weight));
            tooltips.addFirst(LCText.TOOLTIP_SLOT_MACHINE_ODDS.get(odds));
        };
    }

    @Override
    protected void getAdditionalAlertData(TradeContext context, List<AlertData> alerts)
    {
        if(context.hasTrader() && context.getTrader() instanceof SlotMachineTraderData trader)
        {
            if(!trader.isCreative())
            {
                //Check Stock
                if(!trader.hasStock())
                    alerts.add(AlertData.warn(LCText.TOOLTIP_OUT_OF_STOCK));
            }
            //Check whether they can afford the price
            if(!context.hasFunds(this.trade.getCost(context)))
                alerts.add(AlertData.warn(LCText.TOOLTIP_CANNOT_AFFORD));
        }
    }

}
