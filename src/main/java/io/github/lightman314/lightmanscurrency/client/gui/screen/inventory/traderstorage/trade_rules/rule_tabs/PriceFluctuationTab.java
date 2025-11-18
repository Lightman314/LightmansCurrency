package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TimeInputWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.IntParser;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.PriceFluctuation;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;

import javax.annotation.Nonnull;

public class PriceFluctuationTab extends TradeRuleSubTab<PriceFluctuation> {

    public PriceFluctuationTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent, PriceFluctuation.TYPE); }

    TextBoxWrapper<Integer> fluctuationInput;

    TimeInputWidget durationInput;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        PriceFluctuation rule = this.getRule();

        this.fluctuationInput = this.addChild(TextInputUtil.intBuilder()
                .position(screenArea.pos.offset(25,9))
                .size(30,20)
                .maxLength(3)
                .startingValue(rule == null ? 10 : rule.getFluctuation())
                .parser(IntParser.ONE_TO_ONE_HUNDRED)
                .handler(this::onFluctuationChanged)
                .wrap().build());

        this.durationInput = this.addChild(TimeInputWidget.builder()
                .position(screenArea.pos.offset(63,75))
                .unitRange(TimeUtil.TimeUnit.MINUTE, TimeUtil.TimeUnit.DAY)
                .minDuration(TimeUtil.DURATION_MINUTE)
                .handler(this::onTimeSet)
                .startTime(rule == null ? TimeUtil.DURATION_DAY : rule.getDuration())
                .build());

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        PriceFluctuation rule = this.getRule();
        if(rule == null)
            return;

        gui.pushOffset(this.fluctuationInput);
        gui.drawString(LCText.GUI_PRICE_FLUCTUATION_LABEL.get(), this.fluctuationInput.getWidth() + 4, 6, 0x404040);
        gui.popOffset();

        TextRenderUtil.drawCenteredMultilineText(gui, LCText.GUI_PRICE_FLUCTUATION_INFO.get(rule.getFluctuation(), new TimeUtil.TimeData(this.getRule().getDuration()).getShortString()), 10, this.screen.getXSize() - 20, 35, 0x404040);

    }

    void onFluctuationChanged(int newFluctuation)
    {
        PriceFluctuation rule = this.getRule();
        if(rule != null)
            rule.setFluctuation(newFluctuation);
        this.sendUpdateMessage(this.builder().setInt("Fluctuation", newFluctuation));
    }

    public void onTimeSet(TimeUtil.TimeData newTime)
    {
        PriceFluctuation rule = this.getRule();
        if(rule != null)
            rule.setDuration(newTime.miliseconds);
        this.sendUpdateMessage(this.builder().setLong("Duration", newTime.miliseconds));
    }


}
