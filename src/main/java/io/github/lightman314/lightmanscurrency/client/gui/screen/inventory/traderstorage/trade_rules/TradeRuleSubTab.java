package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.common.traders.rules.ITradeRuleHost;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TradeRuleSubTab<T extends TradeRule> extends TradeRulesClientSubTab
{

    public final TradeRuleType<T> ruleType;
    public TradeRuleSubTab(@Nonnull TradeRulesClientTab<?> parent, @Nonnull TradeRuleType<T> ruleType) { super(parent); this.ruleType = ruleType; }

    @Nonnull
    @Override
    public IconData getIcon() {
        T rule = this.getRule();
        return rule != null ? rule.getIcon() : IconData.Null();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected final T getRule()
    {
        ITradeRuleHost host = this.commonTab.getHost();
        if(host != null)
        {
            try{
                return (T)host.getRuleOfType(this.ruleType.type);
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
    public MutableComponent getTooltip() { return TradeRule.nameOfType(this.ruleType.type); }

    public void sendUpdateMessage(@Nonnull LazyPacketData.Builder updateInfo) { this.commonTab.EditTradeRule(this.ruleType, updateInfo); }

}
