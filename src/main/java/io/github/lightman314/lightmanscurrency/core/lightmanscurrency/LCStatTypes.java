package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.stats.StatType;
import io.github.lightman314.lightmanscurrency.api.stats.builtin.IntegerStatType;
import io.github.lightman314.lightmanscurrency.api.stats.builtin.MoneyStatType;
import io.github.lightman314.lightmanscurrency.api.stats.builtin.TimestampStatType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCStatTypes {

    private LCStatTypes() {}

    public static final DeferredRegister<StatType<?,?>> REGISTER = DeferredRegister.create(LCRegistries.Data.STAT_TYPE,LCApi.MODID);

    static {
        register("integer",IntegerStatType.TYPE);
        register("money",MoneyStatType.TYPE);
        register("timestamp",TimestampStatType.TYPE);
    }

    private static void register(String name,StatType<?,?> type) {
        REGISTER.register(name,() -> type);
    }

}
