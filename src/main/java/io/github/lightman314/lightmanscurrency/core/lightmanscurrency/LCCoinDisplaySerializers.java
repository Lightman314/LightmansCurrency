package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.coins.display.ValueDisplaySerializer;
import io.github.lightman314.lightmanscurrency.api.coins.display.builtin.*;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCCoinDisplaySerializers {

    private LCCoinDisplaySerializers() {}

    public static final DeferredRegister<ValueDisplaySerializer> REGISTER = DeferredRegister.create(LCRegistries.Coins.VALUE_DISPLAY_SERIALIZER,LCApi.MODID);

    static {
        register("null",NullDisplay.SERIALIZER);
        register("coin",CoinDisplay.SERIALIZER);
        register("number",NumberDisplay.SERIALIZER);
    }

    private static void register(String name,ValueDisplaySerializer type) {
        REGISTER.register(name,() -> type);
    }

}
