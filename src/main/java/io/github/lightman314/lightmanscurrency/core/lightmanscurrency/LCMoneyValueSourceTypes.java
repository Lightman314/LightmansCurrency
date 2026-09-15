package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.money.values.source.MoneyValueSourceType;
import io.github.lightman314.lightmanscurrency.api.money.values.source.builtin.*;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCMoneyValueSourceTypes {
    private LCMoneyValueSourceTypes() {}

    public static final DeferredRegister<MoneyValueSourceType<?>> REGISTER = DeferredRegister.create(LCRegistries.Money.VALUE_SOURCE,LCApi.MODID);

    static {
        register("direct",DirectSource.TYPE);
        register("configured", ConfiguredSource.TYPE);
    }

    private static void register(String name,MoneyValueSourceType<?> type) {
        REGISTER.register(name,() -> type);
    }

}
