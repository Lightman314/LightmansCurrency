package io.github.lightman314.lightmanscurrency.common.traders.commands.tradedata.client;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.AlertData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.traders.commands.CommandTrader;
import io.github.lightman314.lightmanscurrency.common.traders.commands.tradedata.CommandTrade;
import io.github.lightman314.lightmanscurrency.common.util.TooltipHelper;
import net.minecraftforge.common.util.LazyOptional;

import java.util.List;

public class CommandTradeButtonRenderer extends TradeRenderManager<CommandTrade> {

    public CommandTradeButtonRenderer(CommandTrade trade) { super(trade); }

    @Override
    public int tradeButtonWidth(TradeContext context) { return 189; }

    @Override
    public LazyOptional<ScreenPosition> arrowPosition(TradeContext context) { return ScreenPosition.ofOptional(36, 1); }

    @Override
    public DisplayData inputDisplayArea(TradeContext context) { return new DisplayData(1,1,34,16); }

    @Override
    public List<DisplayEntry> getInputDisplays(TradeContext context) { return this.lazyPriceDisplayList(context); }

    @Override
    public DisplayData outputDisplayArea(TradeContext context) { return new DisplayData(60,1,127,16); }

    @Override
    public List<DisplayEntry> getOutputDisplays(TradeContext context) { return ImmutableList.of(DisplayEntry.of(EasyText.literal(this.trade.getCommandDisplay()),0xFFFFFF,TooltipHelper.splitTooltips(this.trade.getCommandTooltip()))); }

    @Override
    protected void getAdditionalAlertData(TradeContext context, List<AlertData> alerts) {
        if(context.hasTrader() && context.getTrader() instanceof CommandTrader trader)
        {
            //Check whether they can afford the cost
            if(!this.trade.canAfford(context))
                alerts.add(AlertData.warn(LCText.TOOLTIP_CANNOT_AFFORD));
        }
    }

}