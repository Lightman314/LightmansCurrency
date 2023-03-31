package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class RuleToggleTab extends TradeRulesClientSubTab {

    public RuleToggleTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent); }

    private final List<Button> toggleRuleButtons = new ArrayList<>();

    @Override
    public boolean isVisible() { return true; }

    @Override
    public void onOpen() {

        this.toggleRuleButtons.clear();
        int count = this.getFilteredRules().size();
        for(int i = 0; i < count; ++i)
        {
            final int index = i;
            this.toggleRuleButtons.add(this.addWidget(IconAndButtonUtil.checkmarkButton(this.screen.getGuiLeft() + 20, this.screen.getGuiTop() + 25 + (12 * i), this::PressManagerActiveButton, () -> {
                List<TradeRule> rules = this.getFilteredRules();
                if(index < rules.size())
                    return rules.get(index).isActive();
                return false;
            })));
        }

    }

    @Override
    public void onClose() {

    }

    @Override
    public void renderBG(@Nonnull PoseStack pose, int mouseX, int mouseY, float partialTicks) {

        this.font.draw(pose, EasyText.translatable("traderule.list.blurb").withStyle(ChatFormatting.BOLD), this.screen.getGuiLeft() + 20, this.screen.getGuiTop() + 10, 0xFFFFFF);

        List<TradeRule> rules = this.getFilteredRules();
        for(int i = 0; i < this.getFilteredRules().size(); ++i)
        {
            TradeRule rule = rules.get(i);
            MutableComponent name = rule.getName().withStyle(rule.isActive() ? ChatFormatting.GREEN : ChatFormatting.RED).withStyle(ChatFormatting.BOLD);
            this.font.draw(pose, name, this.screen.getGuiLeft() + 32, this.screen.getGuiTop() + 26 + (12 * i), 0xFFFFFF);
        }

    }

    @Override
    public void renderTooltips(@Nonnull PoseStack pose, int mouseX, int mouseY) {

    }

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.of(Items.PAPER); }

    @Override
    public MutableComponent getTooltip() { return EasyText.translatable("gui.button.lightmanscurrency.manager"); }

    void PressManagerActiveButton(Button button)
    {
        int ruleIndex = this.toggleRuleButtons.indexOf(button);
        if(ruleIndex >= 0)
        {
            List<TradeRule> rules = this.getFilteredRules();
            if(ruleIndex < rules.size())
            {
                TradeRule rule = rules.get(ruleIndex);
                CompoundTag updateInfo = new CompoundTag();
                updateInfo.putBoolean("SetActive", !rule.isActive());
                this.commonTab.EditTradeRule(rule.type, updateInfo);
            }
        }
    }

}