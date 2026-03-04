package io.github.lightman314.lightmanscurrency.api.traders.rules.client;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.traders.rules.ITradeRuleHost;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public abstract class TradeRuleSubTab<T extends TradeRule> extends TradeRulesClientSubTab
{

    public final TradeRuleType<T> ruleType;
    public TradeRuleSubTab(TradeRulesClientTab<?> parent, TradeRuleType<T> ruleType) { super(parent); this.ruleType = ruleType; }

    @Override
    public IconData getIcon() {
        T rule = this.getRule();
        return rule != null ? rule.getIcon() : IconData.Null();
    }

    @Nullable
    protected final T getRule()
    {
        ITradeRuleHost host = this.commonTab.getHost();
        if(host != null)
        {
            try{
                return (T)host.getRuleOfType(this.ruleType);
            } catch(Throwable ignored) { }
        }
        return null;
    }

    @Override
    public boolean isVisible()
    {
        T rule = this.getRule();
        if(rule != null)
            return rule.isActive();
        return false;
    }

    @Override
    public Component getTooltip() { return TradeRule.nameOfType(this.ruleType); }

    public void sendUpdateMessage(LazyPacketData.Builder updateInfo) { this.commonTab.EditTradeRule(this.ruleType, updateInfo); }

}
