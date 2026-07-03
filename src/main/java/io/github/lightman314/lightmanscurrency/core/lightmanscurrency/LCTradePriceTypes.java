package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin.*;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCTradePriceTypes {
    private LCTradePriceTypes() {}

    public static final DeferredRegister<TradePriceType<?>> REGISTER = DeferredRegister.create(LCRegistries.Trader.TRADE_PRICE_TYPE,LCApi.MODID);

    static {
        register("money",MoneyPrice.TYPE);
        register("item",ItemPrice.TYPE);
    }

    private static void register(String name,TradePriceType<?> type) {
        REGISTER.register(name,() -> type);
    }

}