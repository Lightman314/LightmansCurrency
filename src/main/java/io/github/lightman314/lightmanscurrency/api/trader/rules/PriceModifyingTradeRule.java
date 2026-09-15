package io.github.lightman314.lightmanscurrency.api.trader.rules;

import javax.annotation.OverridingMethodsMustInvokeSuper;

public abstract class PriceModifyingTradeRule extends TradeRule {

    protected PriceModifyingTradeRule() {}
    protected PriceModifyingTradeRule(boolean active) { super(active); }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected boolean allowHolder(TradeRuleHolder holder) { return holder.canMoneyBeRelevant(); }
    @Override
    @OverridingMethodsMustInvokeSuper
    protected boolean canActivate(TradeRuleHolder holder) { return holder.isMoneyRelevant(); }

}