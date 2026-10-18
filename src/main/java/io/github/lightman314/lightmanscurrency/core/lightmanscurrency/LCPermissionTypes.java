package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.PermissionType;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.builtin.*;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCPermissionTypes {
    public static final DeferredRegister<PermissionType<?>> REGISTER = DeferredRegister.create(LCRegistries.Trader.PERMISSION_TYPE,LCApi.MODID);

    static {
        register("toggle", BooleanPermissionType.INSTANCE);
        register("level", IntegerPermissionType.INSTANCE);
        register("tri_state", EnumPermissionType.TRI_STATE);
    }

    private static void register(String name,PermissionType<?> type) {
        REGISTER.register(name,() -> type);
    }

}