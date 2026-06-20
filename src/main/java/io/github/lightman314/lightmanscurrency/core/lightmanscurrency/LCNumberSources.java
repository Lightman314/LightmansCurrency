package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.upgrades.data.ConfigNumberSource;
import io.github.lightman314.lightmanscurrency.api.upgrades.data.DirectNumberSource;
import io.github.lightman314.lightmanscurrency.api.upgrades.data.NumberSourceType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCNumberSources {
    private LCNumberSources() {}

    public static final DeferredRegister<NumberSourceType<?>> REGISTER = DeferredRegister.create(LCRegistries.Upgrades.NUMBER_SOURCE,LCApi.MODID);

    static {
        register("direct",DirectNumberSource.TYPE);
        register("config",ConfigNumberSource.TYPE);
    }

    private static void register(String name,NumberSourceType<?> type) {
        REGISTER.register(name,() -> type);
    }

}