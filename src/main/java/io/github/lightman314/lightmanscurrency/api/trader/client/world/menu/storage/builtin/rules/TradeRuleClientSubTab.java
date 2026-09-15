package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules;

import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageClientTabWithSubTabs;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleHolder;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.rules.AbstractTradeRuleTab;

import java.util.List;
import java.util.Map;

public abstract class TradeRuleClientSubTab extends TraderStorageClientTabWithSubTabs.SubTab<AbstractTradeRuleTab> {

    protected TradeRuleClientSubTab(TradeRulesClientTab parent) { super(parent); }

    public final Map<TradeRuleType<?>, TradeRule> getRuleMap() {
        TradeRuleHolder holder = this.getCommonTab().getRuleHolder();
        if(holder != null)
            return holder.getRuleData();
        return Map.of();
    }

    public final List<TradeRule> getTradeRules() {
        TradeRuleHolder holder = this.getCommonTab().getRuleHolder();
        if(holder != null)
            return holder.getRules();
        return List.of();
    }

    public final List<TradeRule> getFilteredRules() { return this.filterRules(this.getTradeRules()); }

    protected final List<TradeRule> filterRules(List<TradeRule> rules) { return rules.stream().filter(r -> r.canPlayerActivate(this.getPlayer())).toList(); }

}
