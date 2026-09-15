package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.SpriteButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.IScrollable;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollArea;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.VerticalScrollBar;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.icon.client.IconRenderer;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.rules.AbstractTradeRuleTab;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import java.util.List;

public class RuleToggleTab extends TradeRuleClientSubTab implements IScrollable {

    public static final int RULES_PER_PAGE = 6;

    public RuleToggleTab(TradeRulesClientTab parent) { super(parent); }

    private int scroll = 0;
    @Override
    public int getScroll() { return this.scroll; }

    @Override
    public void setScroll(int scroll) { this.scroll = scroll; }

    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(this.getFilteredRules().size(),RULES_PER_PAGE); }

    @Override
    protected void initialize(ScreenArea area) {
        for(int i = 0; i < RULES_PER_PAGE; ++i) {
            final int index = i;
            this.addChild(SpriteButton.builder()
                    .atPos(area.pos.offset(20,25 + (18 * i)))
                    .onPress(() -> this.toggleRuleActive(index))
                    .withSprite(LCSprites.TOGGLE_COLORED.buildSprite(() -> this.isRuleActive(index)))
                    .visible(() -> this.isValidRuleIndex(index))
                    .build());
        }

        //Scroll Bar & Area
        this.addChild(VerticalScrollBar.builder(this)
                .atPos(area.pos.offset(area.width - 20,25))
                .ofHeight(18 * RULES_PER_PAGE)
                .build());

        this.addChild(ScrollArea.builder()
                .withListener(this.buildScrollListener())
                .atPosition(area.pos)
                .ofSize(area.width,150)
                .build());

        //Back Button
        this.addChild(IconButton.builder()
                .atPos(area.pos.offset(area.width - 25,5))
                .onPress(this.getCommonTab()::goBack)
                .withIcon(SpriteIcon.of(LCApi.id("icon/arrow_back")))
                .visible(() -> this.getCommonTab().getPreviousTab() != null)
                .build());

    }

    @Override
    public IconData getIcon() { return ItemIcon.of(Items.PAPER); }

    @Override
    public Component getName() { return AbstractTradeRuleTab.TOOLTIP_TRADE_RULES_MANAGER.get(); }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {

        gui.text(AbstractTradeRuleTab.GUI_TRADE_RULES_LIST.get(),20,10,0xFF404040,false);

        List<TradeRule> rules = this.getFilteredRules();
        for(int i = this.scroll; i < RULES_PER_PAGE - this.scroll && i < rules.size(); ++i) {
            TradeRule rule = rules.get(i);
            IconRenderer.extractState(gui,rule.getIcon(),30,26 + (18 * i));
            gui.text(rule.getName(),48,30 + (18 * i),0xFF404040,false);
        }

    }

    private boolean isRuleActive(int index) {
        List<TradeRule> rules = this.getFilteredRules();
        index += this.scroll;
        if(index >= 0 && index < rules.size())
            return rules.get(index).isActive();
        return false;
    }

    private boolean isValidRuleIndex(int index) {
        index += this.scroll;
        return index >= 0 && index < this.getFilteredRules().size();
    }

    private void toggleRuleActive(int index) {
        List<TradeRule> rules = this.getFilteredRules();
        index += this.scroll;
        if(index >= 0 && index < rules.size()) {
            TradeRule rule = rules.get(index);
            this.getCommonTab().requestRuleChange(rule.getType(),FancyPacketMap.map().setBoolean("setActive",!rule.isActive()));
        }
    }

}
