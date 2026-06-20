package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.coins.value.CoinValue;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValueType;
import io.github.lightman314.lightmanscurrency.api.money.values.impl.EmptyValue;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCMoneyValueTypes {

    private LCMoneyValueTypes() {}

    public static final DeferredRegister<MoneyValueType<?>> REGISTER = DeferredRegister.create(LCRegistries.Money.VALUE_TYPE,LCApi.MODID);

    static {
        register("empty",EmptyValue.EMPTY_TYPE);
        register("free",EmptyValue.FREE_TYPE);
        register("coins",CoinValue.TYPE);
        //register("ancient_coins",AncientMoneyType.TYPE);
    }

    private static void register(String name,MoneyValueType<?> type) { REGISTER.register(name,() -> type); }

}
