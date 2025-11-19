package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TimeInputWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.IntParser;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.TimedSale;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class TimedSaleTab extends TradeRuleSubTab<TimedSale> {

    public TimedSaleTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent, TimedSale.TYPE); }

    TextBoxWrapper<Integer> discountInput;

    EasyButton buttonSetDiscount;
    EasyButton buttonStartSale;

    TimeInputWidget durationInput;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        TimedSale rule = this.getRule();

        this.discountInput = this.addChild(TextInputUtil.intBuilder()
                .position(screenArea.pos.offset(25,9))
                .size(30,20)
                .maxLength(3)
                .parser(IntParser.ONE_TO_ONE_HUNDRED)
                .startingValue(rule == null ? 1 : rule.getDiscount())
                .handler(this::onDiscountChanged)
                .wrap().build());

        this.buttonStartSale = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(25,45))
                .width(156)
                .text(this::getButtonText)
                .pressAction(this::PressStartButton)
                .addon(EasyAddonHelper.tooltip(this::getButtonTooltip))
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

        if(getRule() == null)
            return;

        gui.pushOffset(this.discountInput);
        gui.drawString(LCText.GUI_PLAYER_DISCOUNTS_INFO.get(), this.discountInput.getWidth() + 4, 6, 0x404040);
        gui.popOffset();

        Component infoText = LCText.GUI_TIMED_SALE_INFO_INACTIVE.get(new TimeUtil.TimeData(this.getRule().getDuration()).getShortString());
        if(this.getRule().timerActive())
            infoText = LCText.GUI_TIMED_SALE_INFO_ACTIVE.get(this.getRule().getTimeRemaining().getShortString(3));

        gui.drawString(infoText, 25, 35, 0x404040);

    }

    @Override
    public void tick() {
        this.buttonStartSale.setMessage(getButtonText());
        TimedSale rule = this.getRule();
        this.buttonStartSale.active = rule != null && (rule.timerActive() || (rule.getDuration() > 0 && rule.isActive()));
        if(rule.getStartTime() != 0 && !rule.timerActive())
        {
            rule.setStartTime(0);
            this.sendUpdateMessage(this.builder().setFlag("StopSale"));
        }
    }

    private Component getButtonText()
    {
        TimedSale rule = this.getRule();
        return rule != null && rule.timerActive() ? LCText.BUTTON_TIMED_SALE_STOP.get() : LCText.BUTTON_TIMED_SALE_START.get();
    }

    private Component getButtonTooltip()
    {
        TimedSale rule = this.getRule();
        return rule != null && rule.timerActive() ? LCText.TOOLTIP_TIMED_SALE_STOP.get() : LCText.TOOLTIP_TIMED_SALE_START.get();
    }

    void onDiscountChanged(int newDiscount)
    {
        TimedSale rule = this.getRule();
        if(rule != null)
            rule.setDiscount(newDiscount);
        this.sendUpdateMessage(this.builder().setInt("Discount", newDiscount));
    }

    void PressStartButton()
    {
        TimedSale rule = this.getRule();
        boolean setActive = rule != null && !rule.timerActive();
        if(rule.timerActive())
        {
            rule.setStartTime(0);
            this.sendUpdateMessage(this.builder().setFlag("StopSale"));
        }
        else
        {
            rule.setStartTime(TimeUtil.getCurrentTime());
            this.sendUpdateMessage(this.builder().setFlag("StartSale"));
        }
    }

    public void onTimeSet(TimeUtil.TimeData newTime)
    {
        TimedSale rule = this.getRule();
        if(rule != null)
            rule.setDuration(newTime.miliseconds);
        this.sendUpdateMessage(this.builder().setLong("Duration", newTime.miliseconds));
    }

}