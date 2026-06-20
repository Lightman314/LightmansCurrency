package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.coins.money.CoinValueHelper;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValueHelper;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCMoneyValueHelpers {

    private LCMoneyValueHelpers() {}

    public static final DeferredRegister<MoneyValueHelper> REGISTER = DeferredRegister.create(LCRegistries.Money.VALUE_HELPER,LCApi.MODID);

    static {
        register("default",MoneyValueHelper.DEFAULT);
        register("coins",CoinValueHelper.INSTANCE);
        //TODO custom ancient coin helper
    }

    private static void register(String name,MoneyValueHelper helper) { REGISTER.register(name,() -> helper); }

}
