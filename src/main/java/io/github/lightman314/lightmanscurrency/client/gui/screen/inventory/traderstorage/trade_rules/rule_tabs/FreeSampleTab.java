package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.FreeSample;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;

import javax.annotation.Nonnull;

public class FreeSampleTab extends TradeRuleSubTab<FreeSample> {

    public FreeSampleTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent, FreeSample.TYPE); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconUtil.ICON_FREE_SAMPLE; }

    EasyButton buttonClearMemory;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.buttonClearMemory = this.addChild(new EasyTextButton(screenArea.pos.offset(10, 60), screenArea.width - 20, 20, LCText.BUTTON_FREE_SAMPLE_RESET.get(), this::PressClearMemoryButton)
                .withAddons(EasyAddonHelper.tooltip(LCText.TOOLTIP_FREE_SAMPLE_RESET)));

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        FreeSample rule = this.getRule();
        if(rule != null)
            TextRenderUtil.drawCenteredMultilineText(gui, LCText.GUI_FREE_SAMPLE_PLAYER_COUNT.get(rule.getSampleCount()), 10, this.screen.getXSize() - 20, 20, 0x404040);
    }

    void PressClearMemoryButton(EasyButton button)
    {
        FreeSample rule = this.getRule();
        if(rule == null)
            return;
        this.sendUpdateMessage(this.builder().setFlag("ClearData"));
    }

}
