package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.IntParser;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.DemandPricing;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class DemandPricingTab extends TradeRuleSubTab<DemandPricing> {

    public DemandPricingTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent, DemandPricing.TYPE); }

    private MoneyValueWidget priceSelection;

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {
        DemandPricing rule = this.getRule();
        this.priceSelection = this.addChild(MoneyValueWidget.builder()
                .position(screenArea.pos.offset(TraderScreen.WIDTH / 2 - MoneyValueWidget.WIDTH / 2,30))
                .oldIfNotFirst(firstOpen,this.priceSelection)
                .startingValue(rule == null ? MoneyValue.empty() : rule.getOtherPrice())
                .valueHandler(this::onValueChanged)
                .typeChangeListener(this::onWidgetHandlerChanged)
                .allowHandlerChange(this::allowHandlerChange)
                .build());
        this.onWidgetHandlerChanged(this.priceSelection);

        this.addChild(TextInputUtil.intBuilder()
                .position(screenArea.pos.offset(10,110))
                .size(80,20)
                .startingValue(rule == null ? 1 : rule.getSmallStock())
                .apply(IntParser.builder()
                        .min(1)
                        .max(this::getSmallStockMax)
                        .empty(1)
                        .consumer())
                .handler(this::smallStockChanged)
                .build());

        this.addChild(TextInputUtil.intBuilder()
                .position(screenArea.pos.offset(10 + (screenArea.width / 2),110))
                .size(80,20)
                .startingValue(rule == null ? 100 : rule.getLargeStock())
                .apply(IntParser.builder()
                        .min(this::getLargeStockMin)
                        .max(DemandPricing.UPPER_STOCK_LIMIT)
                        .empty(() -> this.getSmallStockMax() + 1)
                        .consumer())
                .handler(this::largeStockChanged)
                .build());

        this.tick();
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {
        DemandPricing rule = this.getRule();
        if(rule == null)
            return;

        //Draw Info
        Component infoText = rule.getInfo();
        TextRenderUtil.drawCenteredMultilineText(gui, infoText, 20, this.screen.getXSize() - 40, 6, 0x404040);

        //Draw Stock Input Labels
        gui.drawString(LCText.GUI_DEMAND_PRICING_STOCK_SMALL.get(), 10, 101, 0x404040);
        gui.drawString(LCText.GUI_DEMAND_PRICING_STOCK_LARGE.get(), 10 + (this.screen.getXSize() / 2), 101, 0x404040);

    }

    @Override
    public void tick() {
        DemandPricing rule = this.getRule();
        if(rule == null)
            return;
        if(this.commonTab.getHost() instanceof TradeData trade && this.priceSelection != null)
            this.priceSelection.allowFreeInput = !trade.getCost().isFree() || rule.getOtherPrice().isFree();
    }

    private boolean allowHandlerChange() {
        if(this.commonTab.getHost() instanceof TradeData trade)
        {
            MoneyValue cost = trade.getCost();
            //Allow if price is not yet defined, or is free
            return cost.isEmpty();
        }
        return true;
    }

    private void onWidgetHandlerChanged(@Nonnull MoneyValueWidget widget)
    {
        if(this.commonTab.getHost() instanceof TradeData trade)
        {
            MoneyValue cost = trade.getCost();
            if(cost.isEmpty()) //If trade price is empty or free, we are allowed to determine the upper range
                return;
            //Otherwise attempt to force the current handler to be the same as the trades price
            widget.tryMatchHandler(cost);
        }
    }

    private void onValueChanged(@Nonnull MoneyValue newValue)
    {
        DemandPricing rule = this.getRule();
        if(rule != null)
            rule.setOtherPrice(newValue);
        this.sendUpdateMessage(this.builder().setMoneyValue("ChangePrice",newValue));
    }

    private int getSmallStockMax() {
        DemandPricing rule = this.getRule();
        if(rule == null)
            return DemandPricing.UPPER_STOCK_LIMIT;
        return rule.getLargeStock() - 1;
    }

    private int getLargeStockMin() {
        DemandPricing rule = this.getRule();
        if(rule == null)
            return 2;
        return rule.getSmallStock() + 1;
    }

    private void smallStockChanged(int smallStock)
    {
        DemandPricing rule = this.getRule();
        if(rule == null)
            return;
        if(smallStock != rule.getSmallStock())
        {
            rule.setSmallStock(smallStock);
            this.sendUpdateMessage(this.builder().setInt("ChangeSmallStock",smallStock));
        }
    }

    private void largeStockChanged(int largeStock)
    {
        DemandPricing rule = this.getRule();
        if(rule == null)
            return;
        if(largeStock != rule.getLargeStock())
        {
            rule.setLargeStock(largeStock);
            this.sendUpdateMessage(this.builder().setInt("ChangeLargeStock",largeStock));
        }
    }

}