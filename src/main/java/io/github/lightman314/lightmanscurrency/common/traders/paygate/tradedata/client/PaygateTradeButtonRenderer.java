package io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.client;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.AlertData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.client.TradeRenderManager;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class PaygateTradeButtonRenderer extends TradeRenderManager<PaygateTradeData> {

    public PaygateTradeButtonRenderer(PaygateTradeData trade) { super(trade); }

    @Override
    public int tradeButtonWidth(TradeContext context) { return 94; }

    @Override
    public LazyOptional<ScreenPosition> arrowPosition(TradeContext context) { return ScreenPosition.ofOptional(36, 1); }

    @Override
    public TradeButton.DisplayData inputDisplayArea(TradeContext context) { return new TradeButton.DisplayData(1, 1, 34, 16); }

    @Override
    public List<TradeButton.DisplayEntry> getInputDisplays(TradeContext context) {
        if(this.trade.isTicketTrade())
            return Lists.newArrayList(TradeButton.DisplayEntry.of(TicketItem.CreateTicket(this.trade.getTicketID(), this.trade.getTicketColor()), 1, Lists.newArrayList(Component.translatable("tooltip.lightmanscurrency.ticket.id", this.trade.getTicketID()))));
        else
            return Lists.newArrayList(TradeButton.DisplayEntry.of(this.trade.getCost(context), context.isStorageMode ? Lists.newArrayList(Component.translatable("tooltip.lightmanscurrency.trader.price_edit")) : null));
    }

    @Override
    public TradeButton.DisplayData outputDisplayArea(TradeContext context) { return new TradeButton.DisplayData(58, 1, 34, 16); }

    @Override
    public List<TradeButton.DisplayEntry> getOutputDisplays(TradeContext context) { return Lists.newArrayList(TradeButton.DisplayEntry.of(PaygateTradeData.formatDurationDisplay(this.trade.getDuration()), TextRenderUtil.TextFormatting.create(), Lists.newArrayList(PaygateTradeData.formatDuration(this.trade.getDuration())))); }

    @Override
    protected void getAdditionalAlertData(TradeContext context, List<AlertData> alerts) {
        if(context.hasTrader() && context.getTrader() instanceof PaygateTraderData paygate)
        {
            //Check whether the paygate is currently active
            if(paygate.isActive())
                alerts.add(AlertData.warn(Component.translatable("tooltip.lightmanscurrency.paygate.active")));
            //Check whether they can afford the costs
            if(!this.trade.canAfford(context))
                alerts.add(AlertData.warn(Component.translatable("tooltip.lightmanscurrency.cannotafford")));
        }
    }

}
