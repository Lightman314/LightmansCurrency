package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDataType;
import io.github.lightman314.lightmanscurrency.features.trader.item.trade.ItemTradeData;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCTradeTypes {
    private LCTradeTypes() {}

    public static final DeferredRegister<TradeDataType<?>> REGISTER = DeferredRegister.create(LCRegistries.Trader.TRADE_DATA_TYPE,LCApi.MODID);

    static {
        register("item",ItemTradeData.TYPE);
    }

    private static void register(String name,TradeDataType<?> type) {
        REGISTER.register(name,() -> type);
    }

}
