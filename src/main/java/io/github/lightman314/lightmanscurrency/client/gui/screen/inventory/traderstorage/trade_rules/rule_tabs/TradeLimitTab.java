package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.TradeLimit;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

public class TradeLimitTab extends TradeRuleSubTab<TradeLimit> {

    public TradeLimitTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent, TradeLimit.TYPE); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconAndButtonUtil.ICON_COUNT; }

    EditBox limitInput;
    Button buttonSetLimit;
    Button buttonClearMemory;

    @Override
    public void onOpen() {

        this.limitInput = this.addWidget(new EditBox(this.font, this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 19, 30, 20, EasyText.empty()));
        this.limitInput.setMaxLength(3);
        TradeLimit rule = this.getRule();
        if(rule != null)
            this.limitInput.setValue(Integer.toString(rule.getLimit()));

        this.buttonSetLimit = this.addWidget(EasyButton.builder(EasyText.translatable("gui.button.lightmanscurrency.playerlimit.setlimit"), this::PressSetLimitButton).pos(this.screen.getGuiLeft() + 41, this.screen.getGuiTop() + 19).size(40, 20).build());
        this.buttonClearMemory = this.addWidget(EasyButton.builder(EasyText.translatable("gui.button.lightmanscurrency.playerlimit.clearmemory"), this::PressClearMemoryButton).pos(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 50).size(this.screen.getXSize() - 20, 20).build());

    }

    @Override
    public void onClose() {

    }

    @Override
    public void renderBG(@Nonnull PoseStack pose, int mouseX, int mouseY, float partialTicks) {

        TradeLimit rule = this.getRule();
        if(rule != null)
            this.font.draw(pose, EasyText.translatable("gui.button.lightmanscurrency.playerlimit.info", rule.getLimit()).getString(), this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 9, 0xFFFFFF);


    }

    @Override
    public void renderTooltips(@Nonnull PoseStack pose, int mouseX, int mouseY) {

        if(this.buttonClearMemory.isMouseOver(mouseX, mouseY))
            this.screen.renderTooltip(pose, EasyText.translatable("gui.button.lightmanscurrency.playerlimit.clearmemory.tooltip"), mouseX, mouseY);

    }

    @Override
    public void tick() {
        TextInputUtil.whitelistInteger(this.limitInput, 1, 100);
    }

    void PressSetLimitButton(Button button)
    {
        int limit = MathUtil.clamp(TextInputUtil.getIntegerValue(this.limitInput), 1, 100);
        TradeLimit rule = this.getRule();
        if(rule != null)
            rule.setLimit(limit);
        CompoundTag updateInfo = new CompoundTag();
        updateInfo.putInt("Limit", limit);
        this.sendUpdateMessage(updateInfo);
    }

    void PressClearMemoryButton(Button button)
    {
        TradeLimit rule = this.getRule();
        if(rule != null)
            rule.resetCount();
        CompoundTag updateInfo = new CompoundTag();
        updateInfo.putBoolean("ClearMemory", true);
        this.sendUpdateMessage(updateInfo);
    }

}