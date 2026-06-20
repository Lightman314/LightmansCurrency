package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.data.FancyDataType;
import io.github.lightman314.lightmanscurrency.features.api_impl.data.TraderDataCache;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCFancyDataTypes {

    private LCFancyDataTypes() {}

    public static final DeferredRegister<FancyDataType<?>> REGISTER = DeferredRegister.create(LCRegistries.Data.FANCY_DATA, LCApi.MODID);

    static {
        register("trader",TraderDataCache.TYPE);
    }

    private static void register(String name,FancyDataType<?> type) {
        REGISTER.register(name,() -> type);
    }

}