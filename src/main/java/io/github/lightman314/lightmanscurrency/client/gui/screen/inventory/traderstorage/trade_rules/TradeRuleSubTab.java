package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules;

import io.github.lightman314.lightmanscurrency.common.traders.rules.ITradeRuleHost;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TradeRuleSubTab<T extends TradeRule> extends TradeRulesClientSubTab
{

    public final ResourceLocation ruleType;
    public TradeRuleSubTab(@Nonnull TradeRulesClientTab<?> parent, @Nonnull ResourceLocation ruleType) { super(parent); this.ruleType = ruleType; }

    @Nullable
    @SuppressWarnings("unchecked")
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
    public MutableComponent getTooltip() { return TradeRule.nameOfType(this.ruleType); }

    public void sendUpdateMessage(@Nonnull CompoundTag updateInfo) { this.commonTab.EditTradeRule(this.ruleType, updateInfo); }

}