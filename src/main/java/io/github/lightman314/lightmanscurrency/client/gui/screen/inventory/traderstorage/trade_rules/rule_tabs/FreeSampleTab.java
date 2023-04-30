package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.FreeSample;
import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

public class FreeSampleTab extends TradeRuleSubTab<FreeSample> {

    public FreeSampleTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent, FreeSample.TYPE); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconAndButtonUtil.ICON_FREE_SAMPLE; }

    Button buttonClearMemory;

    @Override
    public void onOpen() {

        this.buttonClearMemory = this.addWidget(EasyButton.builder(EasyText.translatable("gui.button.lightmanscurrency.free_sample.reset"), this::PressClearMemoryButton).pos(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 60).size(screen.getXSize() - 20, 20).build());

    }

    @Override
    public void onClose() {

    }

    @Override
    public void renderBG(@Nonnull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        FreeSample rule = this.getRule();
        if(rule != null)
            TextRenderUtil.drawCenteredMultilineText(pose, EasyText.translatable("gui.lightmanscurrency.free_sample.player_count", rule.getSampleCount()), this.screen.getGuiLeft() + 10, this.screen.getXSize() - 20, this.screen.getGuiTop() + 20, 0x404040);
    }

    @Override
    public void renderTooltips(@Nonnull PoseStack pose, int mouseX, int mouseY) {

        if(this.buttonClearMemory.isMouseOver(mouseX, mouseY))
            screen.renderTooltip(pose, EasyText.translatable("gui.button.lightmanscurrency.free_sample.reset.tooltip"), mouseX, mouseY);

    }

    void PressClearMemoryButton(Button button)
    {
        FreeSample rule = this.getRule();
        if(rule == null)
            return;
        CompoundTag updateInfo = new CompoundTag();
        updateInfo.putBoolean("ClearData", true);
        this.sendUpdateMessage(updateInfo);
    }

}