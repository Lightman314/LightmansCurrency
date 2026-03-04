package io.github.lightman314.lightmanscurrency.api.traders.trade;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Products;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.rules.ITradeRuleHost;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.ITradeListener;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class RuleSupportingTradeData extends TradeData implements ITradeRuleHost, ITradeListener {

    protected static <T extends RuleSupportingTradeData> Products.P2<RecordCodecBuilder.Mu<T>,MoneyValue,Map<TradeRuleType<?>,TradeRule>> ruleFields(RecordCodecBuilder.Instance<T> builder) {
        return builder.group(baseFields(),TradeRule.SET_CODEC.fieldOf("rules").forGetter(RuleSupportingTradeData::getRuleMap));
    }

    private final Map<TradeRuleType<?>,TradeRule> rules = new HashMap<>();
    private final boolean validateRules;
    protected RuleSupportingTradeData() { this(true); }
    protected RuleSupportingTradeData(boolean validateRules) {
        this.validateRules = validateRules;
        TradeRule.AfterRulesLoaded(this.rules,this,this.validateRules);
    }
    protected RuleSupportingTradeData(MoneyValue cost) { this(new HashMap<>(),cost,false); }
    protected RuleSupportingTradeData(Map<TradeRuleType<?>, TradeRule> rules, MoneyValue cost) { this(rules,cost,true); }
    protected RuleSupportingTradeData(Map<TradeRuleType<?>,TradeRule> rules, MoneyValue cost, boolean validateRules) {
        super(cost);
        this.rules.putAll(rules);
        this.validateRules = validateRules;
        TradeRule.AfterRulesLoaded(this.rules,this,this.validateRules);
    }

    public void beforeTrade(TradeEvent.PreTradeEvent event) {
        for(TradeRule rule : this.rules.values())
        {
            if(rule.isActive())
                rule.beforeTrade(event);
        }
    }

    public void tradeCost(TradeEvent.TradeCostEvent event)
    {
        for(TradeRule rule : this.rules.values())
        {
            if(rule.isActive())
                rule.tradeCost(event);
        }
    }

    public void afterTrade(TradeEvent.PostTradeEvent event) {
        for(TradeRule rule : this.rules.values())
        {
            if(rule.isActive())
                rule.afterTrade(event);
        }
    }

    @Override
    public Map<TradeRuleType<?>, TradeRule> getRuleMap() { return ImmutableMap.copyOf(this.rules); }
    @Override
    public boolean isTrader() { return false; }
    @Override
    public boolean isTrade() { return true; }
    @Override
    public void setRuleChanged(TradeRuleType<?> type) {
        this.parent.setTradeRuleChanged(this,type);
    }

    public final void copyRules(RuleSupportingTradeData oldTrade)
    {
        this.rules.putAll(oldTrade.rules);
        //Update the rule host
        TradeRule.AfterRulesLoaded(this.rules,this,false);
    }

    @Nullable
    @Override
    public TradeRule addRule(TradeRuleType<?> type) {
        TradeRule rule = type.create();
        if(rule.allowHost(this) && this.allowTradeRule(rule))
        {
            this.rules.put(type,rule);
            TradeRule.AfterRulesLoaded(this.rules,this,false);
            return rule;
        }
        return null;
    }

    @Override
    @Deprecated
    protected void loadFromNBT(CompoundTag nbt, HolderLookup.Provider lookup) {
        super.loadFromNBT(nbt, lookup);
        this.rules.clear();
        this.rules.putAll(TradeRule.loadOldRules(nbt,"RuleData",this, DataContext.createNBT(lookup)));
        TradeRule.AfterRulesLoaded(this.rules,this,this.validateRules);
    }

    /**
     * Only to be used for persistent trader loading.
     */
    public void setRules(Map<TradeRuleType<?>,TradeRule> rules) {
        this.rules.clear();
        rules.forEach((type,rule) -> {
            if(this.allowTradeRule(rule) && rule.allowHost(this))
                this.rules.put(type,rule);
        });
        TradeRule.AfterRulesLoaded(this.rules,this,false);
    }

}
