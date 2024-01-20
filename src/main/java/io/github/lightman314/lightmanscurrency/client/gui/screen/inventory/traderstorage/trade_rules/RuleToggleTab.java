package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class RuleToggleTab extends TradeRulesClientSubTab {

    public RuleToggleTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent); }

    private final List<EasyButton> toggleRuleButtons = new ArrayList<>();

    @Override
    public boolean isVisible() { return true; }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.toggleRuleButtons.clear();
        int count = this.getFilteredRules().size();
        for(int i = 0; i < count; ++i)
        {
            final int index = i;
            this.toggleRuleButtons.add(this.addChild(IconAndButtonUtil.checkmarkButton(screenArea.pos.offset(20, 25 + (12 * i)), this::PressManagerActiveButton, () -> {
                List<TradeRule> rules = this.getFilteredRules();
                if(index < rules.size())
                    return rules.get(index).isActive();
                return false;
            })));
        }

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        gui.drawString(EasyText.translatable("traderule.list.blurb").withStyle(ChatFormatting.BOLD), 20, 10, 0xFFFFFF);

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
    public MutableComponent getTooltip() { return EasyText.translatable("gui.button.lightmanscurrency.manager"); }

    void PressManagerActiveButton(EasyButton button)
    {
        int ruleIndex = this.toggleRuleButtons.indexOf(button);
        if(ruleIndex >= 0)
        {
            List<TradeRule> rules = this.getFilteredRules();
            if(ruleIndex < rules.size())
            {
                TradeRule rule = rules.get(ruleIndex);
                this.commonTab.EditTradeRule(rule.type, LazyPacketData.simpleBoolean("SetActive",!rule.isActive()));
            }
        }
    }

}
