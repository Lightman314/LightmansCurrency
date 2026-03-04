package io.github.lightman314.lightmanscurrency.common.core.custom;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerType;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.*;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModOwnerTypes {

    public static final DeferredRegister<OwnerType<?>> REGISTER = DeferredRegister.create(LCRegistries.OWNER_TYPES, LightmansCurrency.MODID);

    static {
        register("null",Owner.NULL_TYPE);
        register("fake",FakeOwner.TYPE);
        register("player",PlayerOwner.TYPE);
        register("team",TeamOwner.TYPE);
    }

    public static void register(String name,OwnerType<?> type) {
        REGISTER.register(name,() -> type);
    }

}
