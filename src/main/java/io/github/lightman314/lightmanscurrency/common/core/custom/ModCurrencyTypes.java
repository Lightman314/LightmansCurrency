package io.github.lightman314.lightmanscurrency.common.core.custom;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.CoinCurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.NullCurrencyType;
import io.github.lightman314.lightmanscurrency.common.money.ancient_money.AncientMoneyType;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCurrencyTypes {

    public static final DeferredRegister<CurrencyType<?>> REGISTER = DeferredRegister.create(LCRegistries.CURRENCY_TYPE,LightmansCurrency.MODID);

    static {
        register("null",NullCurrencyType.INSTANCE);
        register("coins",CoinCurrencyType.INSTANCE);
        register("ancient_coins",AncientMoneyType.INSTANCE);
    }

    public static void register(String name,CurrencyType<?> type) { REGISTER.register(name,() -> type); }

}
