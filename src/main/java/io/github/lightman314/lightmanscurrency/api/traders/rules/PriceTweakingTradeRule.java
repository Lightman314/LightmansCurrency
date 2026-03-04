package io.github.lightman314.lightmanscurrency.api.traders.rules;

import javax.annotation.Nullable;

public abstract class PriceTweakingTradeRule extends TradeRule {


    protected PriceTweakingTradeRule() { super(); }
    protected PriceTweakingTradeRule(boolean active) { super(active); }

    @Override
    public boolean allowHost(@Nullable ITradeRuleHost host) { return super.allowHost(host) && host.canMoneyBeRelevant(); }

    @Override
    protected boolean canActivate(@Nullable ITradeRuleHost host) { return host != null && host.isMoneyRelevant(); }

}
