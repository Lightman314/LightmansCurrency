package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs;

import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TimeInputWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.TimedSale;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class TimedSaleTab extends TradeRuleSubTab<TimedSale> {

    public TimedSaleTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent, TimedSale.TYPE); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconAndButtonUtil.ICON_TIMED_SALE; }

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
        this.buttonSetDiscount = this.addChild(new EasyTextButton(screenArea.pos.offset(125, 10), 50, 20, EasyText.translatable("gui.button.lightmanscurrency.discount.set"), this::PressSetDiscountButton));

        this.buttonStartSale = this.addChild(new EasyTextButton(screenArea.pos.offset(25, 45), 156, 20, this::getButtonText, this::PressStartButton)
                .withAddons(EasyAddonHelper.tooltip(this::getButtonTooltip)));

        this.durationInput = this.addChild(new TimeInputWidget(screenArea.pos.offset(63, 75), 10, TimeUtil.TimeUnit.DAY, TimeUtil.TimeUnit.MINUTE, this::onTimeSet));
        this.durationInput.setTime(this.getRule().getDuration());

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        if(getRule() == null)
            return;

        gui.pushOffset(this.discountInput);
        gui.drawString(EasyText.translatable("gui.lightmanscurrency.discount.tooltip"), this.discountInput.getWidth() + 4, 3, 0xFFFFFF);
        gui.popOffset();

        Component infoText = EasyText.translatable("gui.button.lightmanscurrency.timed_sale.info.inactive", new TimeUtil.TimeData(this.getRule().getDuration()).getShortString());
        if(this.getRule().timerActive())
            infoText = EasyText.translatable("gui.button.lightmanscurrency.timed_sale.info.active", this.getRule().getTimeRemaining().getShortString(3));

        gui.drawString(infoText, 25, 35, 0xFFFFFF);

    }

    @Override
    public void tick() {
        this.buttonStartSale.setMessage(getButtonText());
        TimedSale rule = this.getRule();
        this.buttonStartSale.active = rule != null && (rule.timerActive() || (rule.getDuration() > 0 && rule.isActive()));
        TextInputUtil.whitelistInteger(this.discountInput, 0, 99);
    }

    private Component getButtonText()
    {
        TimedSale rule = this.getRule();
        return EasyText.translatable("gui.button.lightmanscurrency.timed_sale." + (rule != null && rule.timerActive() ? "stop" : "start"));
    }

    private Component getButtonTooltip()
    {
        TimedSale rule = this.getRule();
        return EasyText.translatable("gui.button.lightmanscurrency.timed_sale." + (rule != null && rule.timerActive() ? "stop" : "start") + ".tooltip");
    }

    void PressSetDiscountButton(EasyButton button)
    {
        int discount = TextInputUtil.getIntegerValue(this.discountInput, 1);
        TimedSale rule = this.getRule();
        if(rule != null)
            rule.setDiscount(discount);
        CompoundTag updateInfo = new CompoundTag();
        updateInfo.putInt("Discount", discount);
        this.sendUpdateMessage(updateInfo);
    }

    void PressStartButton(EasyButton button)
    {
        TimedSale rule = this.getRule();
        boolean setActive = rule != null && !rule.timerActive();
        if(rule != null)
            rule.setStartTime(rule.timerActive() ? 0 : TimeUtil.getCurrentTime());
        CompoundTag updateInfo = new CompoundTag();
        updateInfo.putBoolean("StartSale", setActive);
        this.sendUpdateMessage(updateInfo);
    }

    public void onTimeSet(TimeUtil.TimeData newTime)
    {
        TimedSale rule = this.getRule();
        if(rule != null)
            rule.setDuration(newTime.miliseconds);
        CompoundTag updateInfo = new CompoundTag();
        updateInfo.putLong("Duration", newTime.miliseconds);
        this.sendUpdateMessage(updateInfo);
    }

}
