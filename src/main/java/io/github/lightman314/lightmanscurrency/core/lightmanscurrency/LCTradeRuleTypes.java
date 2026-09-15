package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.rules.builtin.*;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCTradeRuleTypes {
    private LCTradeRuleTypes() {}

    public static final DeferredRegister<TradeRuleType<?>> REGISTER = DeferredRegister.create(LCRegistries.Trader.TRADE_RULE_TYPE,LCApi.MODID);

    static {
        register("free_sample",FreeSample.TYPE);
        register("player_trade_limit",PlayerTradeLimit.TYPE);
    }

    private static void register(String name,TradeRuleType<?> type) {
        REGISTER.register(name,() -> type);
    }

}
