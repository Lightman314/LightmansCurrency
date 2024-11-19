package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.DemandPricing;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import io.github.lightman314.lightmanscurrency.util.NumberUtil;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class DemandPricingTab extends TradeRuleSubTab<DemandPricing> {

    public DemandPricingTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent, DemandPricing.TYPE); }

    private MoneyValueWidget priceSelection;

    private EditBox smallStockEdit;
    private EditBox largeStockEdit;

    @Nonnull
    @Override
    public IconData getIcon() { return IconUtil.ICON_DEMAND_PRICING; }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {
        DemandPricing rule = this.getRule();
        this.priceSelection = this.addChild(MoneyValueWidget.builder()
                .position(screenArea.pos.offset(TraderScreen.WIDTH / 2 - MoneyValueWidget.WIDTH / 2,30))
                .oldIfNotFirst(firstOpen,this.priceSelection)
                .startingValue(rule == null ? MoneyValue.empty() : rule.getOtherPrice())
                .valueHandler(this::onValueChanged)
                .typeChangeListener(this::onWidgetHandlerChanged)
                .build());
        this.onWidgetHandlerChanged(this.priceSelection);

        this.smallStockEdit = this.addChild(new EditBox(this.getFont(), screenArea.pos.x + 10, screenArea.pos.y + 110, 80, 20, this.smallStockEdit, EasyText.empty()));
        this.smallStockEdit.setValue(rule == null ? "1" : String.valueOf(rule.getSmallStock()));
        this.smallStockEdit.setResponder(this::smallStockChanged);

        this.largeStockEdit = this.addChild(new EditBox(this.getFont(), screenArea.pos.x + 10 + (screenArea.width / 2), screenArea.pos.y + 110, 80, 20, this.largeStockEdit, EasyText.empty()));
        this.largeStockEdit.setValue(rule == null ? "100" : String.valueOf(rule.getLargeStock()));
        this.largeStockEdit.setResponder(this::largeStockChanged);

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
        if(this.smallStockEdit != null)
            TextInputUtil.whitelistInteger(this.smallStockEdit);
        if(this.largeStockEdit != null)
            TextInputUtil.whitelistInteger(this.largeStockEdit);
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

    private void smallStockChanged(@Nonnull String newValue)
    {
        DemandPricing rule = this.getRule();
        if(rule == null)
            return;
        int smallStock = NumberUtil.GetIntegerValue(newValue,1);
        if(smallStock != rule.getSmallStock())
        {
            rule.setSmallStock(smallStock);
            this.sendUpdateMessage(this.builder().setInt("ChangeSmallStock",smallStock));
        }
    }

    private void largeStockChanged(@Nonnull String newValue)
    {
        DemandPricing rule = this.getRule();
        if(rule == null)
            return;
        int largeStock = NumberUtil.GetIntegerValue(newValue,1);
        if(largeStock != rule.getLargeStock())
        {
            rule.setLargeStock(largeStock);
            this.sendUpdateMessage(this.builder().setInt("ChangeLargeStock",largeStock));
        }
    }

}
