package io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.client;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.AlertData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.common.util.TooltipHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class PaygateTradeButtonRenderer extends TradeRenderManager<PaygateTradeData> {

    public PaygateTradeButtonRenderer(PaygateTradeData trade) { super(trade); }

    @Override
    public int tradeButtonWidth(TradeContext context) { return this.trade.getDescription().isBlank() ? 94 : 189; }

    @Override
    public Optional<ScreenPosition> arrowPosition(TradeContext context) { return ScreenPosition.ofOptional(36, 1); }

    @Override
    public DisplayData inputDisplayArea(TradeContext context) { return new DisplayData(1, 1, 34, 16); }

    @Override
    public List<DisplayEntry> getInputDisplays(TradeContext context) {
        if(this.trade.isTicketTrade())
            return Lists.newArrayList(DisplayEntry.of(TicketItem.CreateTicket(this.trade.getTicketItem(), this.trade.getTicketID(), this.trade.getTicketColor()), 1, LCText.TOOLTIP_TICKET_ID.getAsList(this.trade.getTicketID())));
        else
            return this.lazyPriceDisplayList(context,this.getTicketPriceTooltip(context));
    }

    private Component getTicketPriceTooltip(TradeContext context)
    {
        return context.isStorageMode ? LCText.TOOLTIP_TRADE_PAYGATE_SET_TICKET_PRICE.getWithStyle(ChatFormatting.YELLOW) : null;
    }

    @Override
    public DisplayData outputDisplayArea(TradeContext context) { return new DisplayData(58, 1, this.trade.getDescription().isBlank() ? 34 : 127, 16); }

    @Override
    public List<DisplayEntry> getOutputDisplays(TradeContext context) { return Lists.newArrayList(this.trade.getDescription().isBlank() ? DisplayEntry.of(PaygateTradeData.formatDurationDisplay(this.trade.getDuration()), TextRenderUtil.TextFormatting.create(), Lists.newArrayList(PaygateTradeData.formatDuration(this.trade.getDuration()),LCText.GUI_TRADER_PAYGATE_LEVEL.get(this.trade.getRedstoneLevel()))) : DisplayEntry.of(EasyText.literal(this.trade.getDescription()),0xFFFFFF,TooltipHelper.splitTooltips(this.trade.getDescriptionTooltip()))); }

    @Override
    protected void getAdditionalAlertData(TradeContext context, List<AlertData> alerts) {
        if(context.hasTrader() && context.getTrader() instanceof PaygateTraderData paygate)
        {
            //Check whether the paygate is currently active
            if(paygate.isActive())
                alerts.add(AlertData.warn(LCText.TOOLTIP_TRADER_PAYGATE_ALREADY_ACTIVE));
            //Check whether they can afford the costs
            if(!this.trade.canAfford(context))
                alerts.add(AlertData.warn(LCText.TOOLTIP_CANNOT_AFFORD));
        }
    }

}
