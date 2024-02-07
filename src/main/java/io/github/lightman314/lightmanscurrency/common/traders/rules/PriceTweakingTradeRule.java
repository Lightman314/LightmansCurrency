package io.github.lightman314.lightmanscurrency.common.traders.rules;

import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public abstract class PriceTweakingTradeRule extends TradeRule {


    protected PriceTweakingTradeRule(@Nonnull TradeRuleType<?> type) { super(type); }

    @Override
    protected boolean allowHost(@Nonnull ITradeRuleHost host) { return host.canMoneyBeRelevant(); }

    @Override
    protected boolean canActivate(@Nullable ITradeRuleHost host) { return host != null && host.isMoneyRelevant(); }

}
