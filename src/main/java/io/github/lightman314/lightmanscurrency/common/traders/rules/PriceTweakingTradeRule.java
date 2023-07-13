package io.github.lightman314.lightmanscurrency.common.traders.rules;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public abstract class PriceTweakingTradeRule extends TradeRule {


    protected PriceTweakingTradeRule(ResourceLocation type) { super(type); }

    @Override
    protected boolean allowHost(@Nonnull ITradeRuleHost host) { return host.canMoneyBeRelevant(); }

    @Override
    protected boolean canActivate(@Nullable ITradeRuleHost host) { return host != null && host.isMoneyRelevant(); }

}
