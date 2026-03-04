package io.github.lightman314.lightmanscurrency.common.core.custom;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.traders.rules.builtin.*;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModTradeRules {

    public static final DeferredRegister<TradeRuleType<?>> REGISTER;

    static {
        REGISTER = DeferredRegister.create(LCRegistries.TRADE_RULE,LightmansCurrency.MODID);

        REGISTER.register("daily_trades", () -> DailyTrades.TYPE);
        REGISTER.register("demand_pricing", () -> DemandPricing.TYPE);
        REGISTER.register("discount_code", () -> DiscountCodes.TYPE);
        REGISTER.register("free_sample", () -> FreeSample.TYPE);
        REGISTER.register("discount_list", () -> PlayerDiscounts.TYPE);
        REGISTER.register("player_list", () -> PlayerListing.TYPE);
        REGISTER.register("player_trade_limit", () -> PlayerListing.TYPE);
        REGISTER.register("price_fluctuation", () -> PriceFluctuation.TYPE);
        REGISTER.register("timed_sale", () -> TimedSale.TYPE);
        REGISTER.register("trade_limit", () -> TradeLimit.TYPE);

    }

}
