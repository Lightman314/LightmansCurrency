package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageClientTabWithSubTabs;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleHolder;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.rules.AbstractTradeRuleTab;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class TradeRulesClientTab extends TraderStorageClientTabWithSubTabs<AbstractTradeRuleTab,TradeRuleClientSubTab> {

    public static final TabBuilder<TraderStorageMenu,AbstractTradeRuleTab, TraderStorageTab,TraderStorageScreen> BUILDER = TradeRulesClientTab::new;

    protected TradeRulesClientTab(TraderStorageMenu menu,AbstractTradeRuleTab commonTab,TraderStorageScreen screen) {
        super(menu, commonTab, screen);
    }

    @Override
    public IconData getIcon() { return ItemIcon.of(Items.BOOK); }
    @Override
    public Component getName() { return AbstractTradeRuleTab.TOOLTIP_TRADER_TRADE_RULES_TRADER.get(); }

    @Override
    public boolean isVisible() { return super.isVisible() && !this.getCommonTab().hasSpecialOpenRequirement(); }

    @Override
    protected void initialize(ScreenArea area, FancyPacketMap message) {
        super.initialize(area, message);
        this.addScrollArrows(area);
    }

    @Override
    protected void collectSubtabs(Consumer<TradeRuleClientSubTab> builder) {
        //First add the rule toggle tab
        builder.accept(new RuleToggleTab(this));
        //Then add the trade rule tabs
        TradeRuleHolder holder = this.getCommonTab().getRuleHolder();
        if(holder != null) {
            for(TradeRuleType<?> type : holder.getRuleData().keySet()) {
                RuleClientTab<?> tab = RuleClientTab.TAB_BUILDERS.getValue(type).apply(this);
                if(tab != null)
                    builder.accept(tab);
                else
                    LightmansCurrency.LogWarning("Missing Client Trade Rule tab for rule of type " + type);
            }
        }
    }

}