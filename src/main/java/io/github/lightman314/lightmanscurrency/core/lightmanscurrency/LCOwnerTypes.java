package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerType;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.*;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCOwnerTypes {

    private LCOwnerTypes() {}

    public static final DeferredRegister<OwnerType<?>> REGISTER = DeferredRegister.create(LCRegistries.Ownership.OWNER_TYPE,LCApi.MODID);

    static {
        register("null",Owner.NULL_TYPE);
        register("fake",FakeOwner.TYPE);
        register("player",PlayerOwner.TYPE);
        //register("team",TeamOwner.TYPE);
    }

    private static void register(String name,OwnerType<?> type) {
        REGISTER.register(name,() -> type);
    }

}
