package io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade_data.client;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyScreenHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.AlertData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade_data.SlotMachineTrade;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.client.TradeRenderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;

import java.util.ArrayList;
import java.util.List;

public class SlotMachineTradeButtonRenderer extends TradeRenderManager<SlotMachineTrade> {

    public SlotMachineTradeButtonRenderer(SlotMachineTrade trade) { super(trade); }

    @Override
    public int tradeButtonWidth(TradeContext context) { return 128; }

    @Override
    public LazyOptional<ScreenPosition> arrowPosition(TradeContext context) { return ScreenPosition.ofOptional(36, 1); }

    @Override
    public TradeButton.DisplayData inputDisplayArea(TradeContext context) { return new TradeButton.DisplayData(1, 1, 34, 16); }

    @Override
    public List<TradeButton.DisplayEntry> getInputDisplays(TradeContext context) { return Lists.newArrayList(TradeButton.DisplayEntry.of(this.trade.getCost(context))); }

    @Override
    public TradeButton.DisplayData outputDisplayArea(TradeContext context) { return new TradeButton.DisplayData(59, 1, 68, 16); }

    private SlotMachineEntry getTimedEntry()
    {
        List<SlotMachineEntry> entries = this.trade.trader.getValidEntries();
        if(entries.size() == 0)
            return null;
        return entries.get((int)Minecraft.getInstance().level.getGameTime()/20 % entries.size());
    }

    @Override
    public List<TradeButton.DisplayEntry> getOutputDisplays(TradeContext context) {
        SlotMachineEntry entry = this.getTimedEntry();
        if(entry == null)
            return new ArrayList<>();
        List<TradeButton.DisplayEntry> entries = new ArrayList<>();
        String odds = this.trade.trader.getOdds(entry.getWeight());
        for(ItemStack item : entry.items)
            entries.add(TradeButton.DisplayEntry.of(item, item.getCount(), this.getTooltip(item, entry.getWeight(), odds)));
        return entries;
    }

    private List<Component> getTooltip(ItemStack stack, int weight, String odds)
    {
        if(stack.isEmpty())
            return null;

        List<Component> tooltips = EasyScreenHelper.getTooltipFromItem(stack);
        tooltips.add(0, EasyText.translatable("tooltip.lightmanscurrency.slot_machine.weight", weight));
        tooltips.add(0, EasyText.translatable("tooltip.lightmanscurrency.slot_machine.odds", odds));

        return tooltips;

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
                    alerts.add(AlertData.warn(EasyText.translatable("tooltip.lightmanscurrency.outofstock")));
            }
            //Check whether they can afford the price
            if(!context.hasFunds(this.trade.getCost(context)))
                alerts.add(AlertData.warn(EasyText.translatable("tooltip.lightmanscurrency.cannotafford")));
        }
    }

}
