package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RuleToggleTab extends TradeRulesClientSubTab implements IScrollable {

    public RuleToggleTab(TradeRulesClientTab<?> parent) { super(parent); }

    @Override
    public boolean isVisible() { return true; }

    public static final int RULES_PER_PAGE = 6;

    private int scroll = 0;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        //Always make all 9 checkmarks and let the visibility check hide the unecessary ones
        //Done like this because sometimes the
        for(int i = 0; i < RULES_PER_PAGE; ++i)
        {
            final int index = i;
            this.addChild(PlainButton.builder()
                    .position(screenArea.pos.offset(20,25 + (18 * i)))
                    .pressAction(() -> this.PressManagerActiveButton(index))
                    .sprite(SpriteUtil.createColoredToggle(() -> this.isRuleActive(index)))
                    .addon(EasyAddonHelper.visibleCheck(() -> this.isValidRuleIndex(index)))
                    .build());
        }

        this.addChild(ScrollBarWidget.builder()
                .position(screenArea.pos.offset(screenArea.width - 20,25))
                .height(18 * RULES_PER_PAGE)
                .scrollable(this)
                .build());

        this.addChild(ScrollListener.builder()
                .position(screenArea.pos)
                .size(screenArea.width,150)
                .listener(this)
                .build());

        this.addChild(IconButton.builder()
                .position(screenArea.pos.offset(screenArea.width - 25, 5))
                .pressAction(this::ClickBackButton)
                .icon(IconUtil.ICON_BACK)
                .addon(EasyAddonHelper.visibleCheck(this.commonTab::hasBackButton))
                .build());

    }

    @Override
    public void renderBG(EasyGuiGraphics gui) {

        gui.drawString(LCText.GUI_TRADE_RULES_LIST.get(), 20, 10, 0x404040);

        List<TradeRule> rules = this.getFilteredRules();
        for(int i = 0; i < RULES_PER_PAGE; ++i)
        {
            int index = i + this.scroll;
            if(index < rules.size())
            {
                TradeRule rule = rules.get(index);
                rule.getIcon().render(gui, 30, 26 + (18 * i));
                gui.drawString(rule.getName(), 48, 30 + (18 * i), 0x404040);
            }
        }

    }

    @Override
    public IconData getIcon() { return ItemIcon.ofItem(Items.PAPER); }

    @Override
    public MutableComponent getTooltip() { return LCText.TOOLTIP_TRADE_RULES_MANAGER.get(); }

    private boolean isRuleActive(int ruleIndex)
    {
        List<TradeRule> rules = this.getFilteredRules();
        ruleIndex += this.scroll;
        if(ruleIndex < rules.size())
            return rules.get(ruleIndex).isActive();
        return false;
    }

    private boolean isValidRuleIndex(int ruleIndex) {
        ruleIndex += this.scroll;
        return ruleIndex >= 0 && ruleIndex < this.getFilteredRules().size();
    }

    void PressManagerActiveButton(int ruleIndex)
    {
        List<TradeRule> rules = this.getFilteredRules();
        ruleIndex += this.scroll;
        if(ruleIndex < rules.size())
        {
            TradeRule rule = rules.get(ruleIndex);
            this.commonTab.EditTradeRule(rule.type, this.builder().setBoolean("SetActive",!rule.isActive()));
        }
    }

    private void ClickBackButton(EasyButton button) { this.commonTab.goBack(); }

    @Override
    public int currentScroll() { return this.scroll; }
    @Override
    public void setScroll(int newScroll) { this.scroll = newScroll; }
    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(RULES_PER_PAGE,this.getTradeRules().size()); }

}
