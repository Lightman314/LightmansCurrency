package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TimeInputWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.TimedSale;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.client.gui.components.Button;
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

    Button buttonSetDiscount;
    Button buttonStartSale;

    TimeInputWidget durationInput;

    @Override
    public void onOpen() {

        this.discountInput = this.addWidget(new EditBox(this.font, this.screen.getGuiLeft() + 25, this.screen.getGuiTop() + 9, 20, 20, Component.empty()));
        this.discountInput.setMaxLength(2);
        TimedSale rule = this.getRule();
        if(rule != null)
            this.discountInput.setValue(Integer.toString(rule.getDiscount()));
        this.buttonSetDiscount = this.addWidget(Button.builder(Component.translatable("gui.button.lightmanscurrency.discount.set"), this::PressSetDiscountButton).pos(this.screen.getGuiLeft() + 125, this.screen.getGuiTop() + 10).size(50, 20).build());

        this.buttonStartSale = this.addWidget(Button.builder(this.getButtonText(), this::PressStartButton).pos(this.screen.getGuiLeft() + 25, this.screen.getGuiTop() + 45).size(156, 20).build());

        this.durationInput = this.addWidget(new TimeInputWidget(this.screen.getGuiLeft() + 63, this.screen.getGuiTop() + 75, 10, TimeUtil.TimeUnit.DAY, TimeUtil.TimeUnit.MINUTE, this::addWidget, this::onTimeSet));
        this.durationInput.setTime(this.getRule().getDuration());

    }

    @Override
    public void onClose() {

    }

    @Override
    public void renderBG(@Nonnull PoseStack pose, int mouseX, int mouseY, float partialTicks) {

        if(getRule() == null)
            return;

        this.font.draw(pose, EasyText.translatable("gui.lightmanscurrency.discount.tooltip"), this.discountInput.getX() + this.discountInput.getWidth() + 4, this.discountInput.getY() + 3, 0xFFFFFF);

        Component infoText = EasyText.translatable("gui.button.lightmanscurrency.timed_sale.info.inactive", new TimeUtil.TimeData(this.getRule().getDuration()).getShortString());
        if(this.getRule().timerActive())
            infoText = EasyText.translatable("gui.button.lightmanscurrency.timed_sale.info.active", this.getRule().getTimeRemaining().getShortString(3));

        this.font.draw(pose, infoText.getString(), this.screen.getGuiLeft() + 25, this.screen.getGuiTop() + 35, 0xFFFFFF);

    }

    @Override
    public void renderTooltips(@Nonnull PoseStack pose, int mouseX, int mouseY) {

        if(this.buttonStartSale.isMouseOver(mouseX, mouseY))
            screen.renderTooltip(pose, this.getButtonTooltip(), mouseX, mouseY);

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
        return Component.translatable("gui.button.lightmanscurrency.timed_sale." + (rule != null && rule.timerActive() ? "stop" : "start"));
    }

    private Component getButtonTooltip()
    {
        TimedSale rule = this.getRule();
        return Component.translatable("gui.button.lightmanscurrency.timed_sale." + (rule != null && rule.timerActive() ? "stop" : "start") + ".tooltip");
    }

    void PressSetDiscountButton(Button button)
    {
        int discount = TextInputUtil.getIntegerValue(this.discountInput, 1);
        TimedSale rule = this.getRule();
        if(rule != null)
            rule.setDiscount(discount);
        CompoundTag updateInfo = new CompoundTag();
        updateInfo.putInt("Discount", discount);
        this.sendUpdateMessage(updateInfo);
    }

    void PressStartButton(Button button)
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
