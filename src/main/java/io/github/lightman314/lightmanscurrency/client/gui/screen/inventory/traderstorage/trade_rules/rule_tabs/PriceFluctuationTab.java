package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TimeInputWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.PriceFluctuation;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

public class PriceFluctuationTab extends TradeRuleSubTab<PriceFluctuation> {

    public PriceFluctuationTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent, PriceFluctuation.TYPE); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconAndButtonUtil.ICON_PRICE_FLUCTUATION; }

    EditBox fluctuationInput;
    Button buttonSetFluctuation;

    TimeInputWidget durationInput;

    @Override
    public void onOpen() {

        this.fluctuationInput = this.addWidget(new EditBox(this.font, this.screen.getGuiLeft() + 25, this.screen.getGuiTop() + 9, 20, 20, EasyText.empty()));
        this.fluctuationInput.setMaxLength(2);
        PriceFluctuation rule = this.getRule();
        if(rule != null)
            this.fluctuationInput.setValue(Integer.toString(rule.getFluctuation()));

        this.buttonSetFluctuation = this.addWidget(EasyButton.builder(EasyText.translatable("gui.button.lightmanscurrency.discount.set"), this::PressSetFluctuationButton).pos(this.screen.getGuiLeft() + 125, this.screen.getGuiTop() + 10).size(50, 20).build());

        this.durationInput = this.addWidget(new TimeInputWidget(this.screen.getGuiLeft() + 63, this.screen.getGuiTop() + 75, 10, TimeUtil.TimeUnit.DAY, TimeUtil.TimeUnit.MINUTE, this::addWidget, this::onTimeSet));
        this.durationInput.setTime(this.getRule().getDuration());

    }

    @Override
    public void onClose() {

    }

    @Override
    public void renderBG(@Nonnull PoseStack pose, int mouseX, int mouseY, float partialTicks) {

        PriceFluctuation rule = this.getRule();
        if(rule == null)
            return;

        this.font.draw(pose, EasyText.translatable("gui.lightmanscurrency.fluctuation.tooltip"), this.fluctuationInput.x + this.fluctuationInput.getWidth() + 4, this.fluctuationInput.y + 3, 0xFFFFFF);

        TextRenderUtil.drawCenteredMultilineText(pose, EasyText.translatable("gui.button.lightmanscurrency.price_fluctuation.info", this.getRule().getFluctuation(), new TimeUtil.TimeData(this.getRule().getDuration()).getShortString()), this.screen.getGuiLeft() + 10, this.screen.getXSize() - 20, this.screen.getGuiTop() + 35, 0xFFFFFF);

    }

    @Override
    public void renderTooltips(@Nonnull PoseStack pose, int mouseX, int mouseY) {

    }

    @Override
    public void tick() { TextInputUtil.whitelistInteger(this.fluctuationInput, 1, Integer.MAX_VALUE); }

    void PressSetFluctuationButton(Button button)
    {
        int fluctuation = TextInputUtil.getIntegerValue(this.fluctuationInput, 1);
        PriceFluctuation rule = this.getRule();
        if(rule != null)
            rule.setFluctuation(fluctuation);
        CompoundTag updateInfo = new CompoundTag();
        updateInfo.putInt("Fluctuation", fluctuation);
        this.sendUpdateMessage(updateInfo);
    }

    public void onTimeSet(TimeUtil.TimeData newTime)
    {
        PriceFluctuation rule = this.getRule();
        if(rule != null)
            rule.setDuration(newTime.miliseconds);
        CompoundTag updateInfo = new CompoundTag();
        updateInfo.putLong("Duration", newTime.miliseconds);
        this.sendUpdateMessage(updateInfo);
    }


}