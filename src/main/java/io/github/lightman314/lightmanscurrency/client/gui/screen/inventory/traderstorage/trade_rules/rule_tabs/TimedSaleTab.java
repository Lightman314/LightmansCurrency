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
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.TimedSale;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class TimedSaleTab extends TradeRuleSubTab<TimedSale> {

    public TimedSaleTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent, TimedSale.TYPE); }

    EditBox discountInput;

    EasyButton buttonSetDiscount;
    EasyButton buttonStartSale;

    TimeInputWidget durationInput;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.discountInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 25, screenArea.y + 9, 20, 20, EasyText.empty()));
        this.discountInput.setMaxLength(2);
        TimedSale rule = this.getRule();
        if(rule != null)
            this.discountInput.setValue(Integer.toString(rule.getDiscount()));
        this.buttonSetDiscount = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(125,10))
                .width(50)
                .text(LCText.BUTTON_SET)
                .pressAction(this::PressSetDiscountButton)
                .build());

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
        TextInputUtil.whitelistInteger(this.discountInput, 0, 99);
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

    void PressSetDiscountButton()
    {
        int discount = TextInputUtil.getIntegerValue(this.discountInput, 1);
        TimedSale rule = this.getRule();
        if(rule != null)
            rule.setDiscount(discount);
        this.sendUpdateMessage(this.builder().setInt("Discount", discount));
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
