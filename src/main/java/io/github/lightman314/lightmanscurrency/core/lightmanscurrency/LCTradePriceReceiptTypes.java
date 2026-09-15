package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceReceiptType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin.ItemReceipt;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin.MoneyReceipt;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCTradePriceReceiptTypes {

    private LCTradePriceReceiptTypes() {}

    public static final DeferredRegister<TradePriceReceiptType<?>> REGISTER = DeferredRegister.create(LCRegistries.Trader.TRADE_PRICE_RECEIPT_TYPE,LCApi.MODID);

    static {
        register("money",MoneyReceipt.TYPE);
        register("items",ItemReceipt.TYPE);
    }

    private static void register(String name,TradePriceReceiptType<?> type) {
        REGISTER.register(name,() -> type);
    }

}