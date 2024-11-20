package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TimeInputWidget;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.PriceFluctuation;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.client.gui.components.EditBox;

import javax.annotation.Nonnull;

public class PriceFluctuationTab extends TradeRuleSubTab<PriceFluctuation> {

    public PriceFluctuationTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent, PriceFluctuation.TYPE); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconUtil.ICON_PRICE_FLUCTUATION; }

    EditBox fluctuationInput;
    EasyButton buttonSetFluctuation;

    TimeInputWidget durationInput;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.fluctuationInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 25, screenArea.y + 9, 20, 20, EasyText.empty()));
        this.fluctuationInput.setMaxLength(2);
        PriceFluctuation rule = this.getRule();
        if(rule != null)
            this.fluctuationInput.setValue(Integer.toString(rule.getFluctuation()));

        this.buttonSetFluctuation = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(125,10))
                .width(50)
                .text(LCText.BUTTON_SET)
                .pressAction(this::PressSetFluctuationButton)
                .build());

        this.durationInput = this.addChild(TimeInputWidget.builder()
                .position(screenArea.pos.offset(63,75))
                .unitRange(TimeUtil.TimeUnit.MINUTE, TimeUtil.TimeUnit.DAY)
                .handler(this::onTimeSet)
                .startTime(rule.getDuration())
                .build());

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        PriceFluctuation rule = this.getRule();
        if(rule == null)
            return;

        gui.pushOffset(this.fluctuationInput);
        gui.drawString(LCText.GUI_PRICE_FLUCTUATION_LABEL.get(), this.fluctuationInput.getWidth() + 4, 3, 0xFFFFFF);
        gui.popOffset();

        TextRenderUtil.drawCenteredMultilineText(gui, LCText.GUI_PRICE_FLUCTUATION_INFO.get(rule.getFluctuation(), new TimeUtil.TimeData(this.getRule().getDuration()).getShortString()), 10, this.screen.getXSize() - 20, 35, 0xFFFFFF);

    }

    @Override
    public void tick() { TextInputUtil.whitelistInteger(this.fluctuationInput, 1, Integer.MAX_VALUE); }

    void PressSetFluctuationButton(EasyButton button)
    {
        int fluctuation = TextInputUtil.getIntegerValue(this.fluctuationInput, 1);
        PriceFluctuation rule = this.getRule();
        if(rule != null)
            rule.setFluctuation(fluctuation);
        this.sendUpdateMessage(this.builder().setInt("Fluctuation", fluctuation));
    }

    public void onTimeSet(TimeUtil.TimeData newTime)
    {
        PriceFluctuation rule = this.getRule();
        if(rule != null)
            rule.setDuration(newTime.miliseconds);
        this.sendUpdateMessage(this.builder().setLong("Duration", newTime.miliseconds));
    }

}