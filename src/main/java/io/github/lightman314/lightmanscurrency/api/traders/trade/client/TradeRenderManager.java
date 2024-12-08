package io.github.lightman314.lightmanscurrency.api.traders.trade.client;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.AlertData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
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
    public abstract DisplayData inputDisplayArea(TradeContext context);

    /**
     * The input display entries. For a sale this would be the trades price.
     */
    public abstract List<DisplayEntry> getInputDisplays(TradeContext context);

    /**
     * The position and size of the output displays
     */
    public abstract DisplayData outputDisplayArea(TradeContext context);

    /**
     * The output display entries. For a sale this would be the product being sold.
     */
    public abstract List<DisplayEntry> getOutputDisplays(TradeContext context);

    protected final List<DisplayEntry> lazyPriceDisplayList(TradeContext context) { return Lists.newArrayList(this.lazyPriceDisplay(context)); }

    protected final DisplayEntry lazyPriceDisplay(TradeContext context)
    {
        List<Component> extraTooltips = null;
        if(context.isStorageMode && this.hasPermission(context, Permissions.EDIT_TRADES))
            extraTooltips = LCText.TOOLTIP_TRADE_EDIT_PRICE.getAsListWithStyle(ChatFormatting.YELLOW);
        return DisplayEntry.of(this.trade.getCost(context),extraTooltips);
    }

    protected final boolean hasPermission(TradeContext context, String permission)
    {
        TraderData trader = context.getTrader();
        if(trader == null)
            return false;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        return trader.hasPermission(player,permission);
    }

    protected final int getPermissionLevel(TradeContext context, String permission)
    {
        TraderData trader = context.getTrader();
        if(trader == null)
            return 0;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        return trader.getPermissionLevel(player,permission);
    }

    /**
     * List of alert data. Used for Out of Stock, Cannot Afford, or Trade Rule messages.
     * Return null to display no alert.
     */
    public final List<AlertData> getAlertData(TradeContext context) {
        if(context.isStorageMode)
            return null;
        List<AlertData> alerts = new ArrayList<>();
        this.addTradeRuleAlertData(alerts, context);
        if(context.hasTrader() && context.getTrader().exceedsAcceptableTaxRate())
            alerts.add(AlertData.error(LCText.TOOLTIP_TAX_LIMIT.get()));
        this.getAdditionalAlertData(context, alerts);
        return alerts;
    }

    private void addTradeRuleAlertData(List<AlertData> alerts, TradeContext context) {
        if(context.hasTrader() && context.hasPlayerReference())
        {
            TradeEvent.PreTradeEvent pte = context.getTrader().runPreTradeEvent(this.trade, context);
            alerts.addAll(pte.getAlertInfo());
        }
    }

    protected abstract void getAdditionalAlertData(TradeContext context, List<AlertData> alerts);

    /**
     * Render trade-specific icons for the trade, such as the fluid traders drainable/fillable icons.
     * @param button The button that is rendering the trade
     * @param gui The gui renderBG helper
     * @param context The context of the trade.
     */
    public void renderAdditional(EasyWidget button, EasyGuiGraphics gui, TradeContext context) { }

    /**
     * Render trade-specific tooltips for the trade, such as the fluid traders drainable/fillable icons.
     * @param context The context of the trade.
     * @param mouseX The mouses X position relative to the left edge of the button.
     * @param mouseY The mouses Y position relative to the top edge of the button.
     * @return The list of tooltip text. Return null to display no tooltip.
     */
    public List<Component> getAdditionalTooltips(TradeContext context, int mouseX, int mouseY) { return null; }

    public final MutableComponent getStockTooltip(boolean isCreative, int stockCount)
    {
        return LCText.TOOLTIP_TRADE_INFO_STOCK.get(isCreative ? LCText.TOOLTIP_TRADE_INFO_STOCK_INFINITE.getWithStyle(ChatFormatting.GOLD) : EasyText.literal(String.valueOf(stockCount)).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GOLD);
    }

}
