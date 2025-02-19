package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.List;

public class RuleToggleTab extends TradeRulesClientSubTab {

    public RuleToggleTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent); }

    @Override
    public boolean isVisible() { return true; }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        //Always make all 9 checkmarks and let the visibility check hide the unecessary ones
        //Done like this because sometimes the
        for(int i = 0; i < 9; ++i)
        {
            final int index = i;
            this.addChild(PlainButton.builder()
                    .position(screenArea.pos.offset(20,25 + (12 * i)))
                    .pressAction(() -> this.PressManagerActiveButton(index))
                    .sprite(IconAndButtonUtil.SPRITE_CHECK(() -> this.isRuleActive(index)))
                    .addon(EasyAddonHelper.visibleCheck(() -> this.isValidRuleIndex(index)))
                    .build());
        }

        if(this.commonTab.hasBackButton())
        {
            this.addChild(IconButton.builder()
                    .position(screenArea.pos.offset(screenArea.width - 25, 5))
                    .pressAction(this::ClickBackButton)
                    .icon(IconUtil.ICON_BACK)
                    .build());
        }

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        gui.drawString(LCText.GUI_TRADE_RULES_LIST.getWithStyle(ChatFormatting.BOLD), 20, 10, 0xFFFFFF);

        List<TradeRule> rules = this.getFilteredRules();
        for(int i = 0; i < this.getFilteredRules().size(); ++i)
        {
            TradeRule rule = rules.get(i);
            MutableComponent name = rule.getName().withStyle(rule.isActive() ? ChatFormatting.GREEN : ChatFormatting.RED).withStyle(ChatFormatting.BOLD);
            gui.drawString(name, 32, 26 + (12 * i), 0xFFFFFF);
        }

    }

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.of(Items.PAPER); }

    @Override
    public MutableComponent getTooltip() { return LCText.TOOLTIP_TRADE_RULES_MANAGER.get(); }

    private boolean isRuleActive(int ruleIndex)
    {
        List<TradeRule> rules = this.getFilteredRules();
        if(ruleIndex < rules.size())
            return rules.get(ruleIndex).isActive();
        return false;
    }

    private boolean isValidRuleIndex(int ruleIndex) { return ruleIndex >= 0 && ruleIndex < this.getFilteredRules().size(); }

    void PressManagerActiveButton(int ruleIndex)
    {
        List<TradeRule> rules = this.getFilteredRules();
        if(ruleIndex < rules.size())
        {
            TradeRule rule = rules.get(ruleIndex);
            this.commonTab.EditTradeRule(rule.type, this.builder().setBoolean("SetActive",!rule.isActive()));
        }
    }

    private void ClickBackButton(@Nonnull EasyButton button) { this.commonTab.goBack(); }

}
