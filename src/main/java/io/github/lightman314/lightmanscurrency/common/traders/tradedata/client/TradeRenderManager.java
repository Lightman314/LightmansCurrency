package io.github.lightman314.lightmanscurrency.common.traders.tradedata.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.AlertData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public abstract class TradeRenderManager<T extends TradeData> {

    public final T trade;
    protected TradeRenderManager(T trade) { this.trade = trade; }

    /**
     * The width of the trade button.
     */
    public abstract int tradeButtonWidth(TradeContext context);

    /**
     * Where on the button the arrow should be drawn.
     * Return an empty optional if the arrow should not be drawn.
     */
    public abstract LazyOptional<ScreenPosition> arrowPosition(TradeContext context);

    public ScreenPosition alertPosition(TradeContext context) { return this.arrowPosition(context).orElseGet(() -> ScreenPosition.ZERO); }

    /**
     * The position and size of the input displays
     */
    public abstract TradeButton.DisplayData inputDisplayArea(TradeContext context);

    /**
     * The input display entries. For a sale this would be the trades price.
     */
    public abstract List<TradeButton.DisplayEntry> getInputDisplays(TradeContext context);

    /**
     * The position and size of the output displays
     */
    public abstract TradeButton.DisplayData outputDisplayArea(TradeContext context);

    /**
     * The output display entries. For a sale this would be the product being sold.
     */
    public abstract List<TradeButton.DisplayEntry> getOutputDisplays(TradeContext context);

    /**
     * List of alert data. Used for Out of Stock, Cannot Afford, or Trade Rule messages.
     * Return null to display no alert.
     */
    public final List<AlertData> getAlertData(TradeContext context) {
        if(context.isStorageMode)
            return null;
        List<AlertData> alerts = new ArrayList<>();
        this.addTradeRuleAlertData(alerts, context);
        this.getAdditionalAlertData(context, alerts);
        return alerts;
    }

    private void addTradeRuleAlertData(List<AlertData> alerts, TradeContext context) {
        if(context.hasTrader() && context.hasPlayerReference())
        {
            TradeEvent.PreTradeEvent pte = context.getTrader().runPreTradeEvent(context.getPlayerReference(), this.trade);
            alerts.addAll(pte.getAlertInfo());
        }
    }

    protected abstract void getAdditionalAlertData(TradeContext context, List<AlertData> alerts);

    /**
     * Render trade-specific icons for the trade, such as the fluid traders drainable/fillable icons.
     * @param button The button that is rendering the trade
     * @param pose The pose stack
     * @param mouseX The x position of the mouse.
     * @param mouseY The y position of the mouse.
     * @param context The context of the trade.
     */
    @OnlyIn(Dist.CLIENT)
    public void renderAdditional(AbstractWidget button, PoseStack pose, int mouseX, int mouseY, TradeContext context) { }

    /**
     * Render trade-specific tooltips for the trade, such as the fluid traders drainable/fillable icons.
     * @param context The context of the trade.
     * @param mouseX The mouses X position relative to the left edge of the button.
     * @param mouseY The mouses Y position relative to the top edge of the button.
     * @return The list of tooltip text. Return null to display no tooltip.
     */
    public List<Component> getAdditionalTooltips(TradeContext context, int mouseX, int mouseY) { return null; }


}
