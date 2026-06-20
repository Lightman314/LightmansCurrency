package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.PotentialOwnerProvider;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.builtin.PlayerOwnerProvider;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCOwnerProviders {

    private LCOwnerProviders() {}

    public static final DeferredRegister<PotentialOwnerProvider> REGISTER = DeferredRegister.create(LCRegistries.Ownership.POTENTIAL_OWNER,LCApi.MODID);

    static {
        register("player",PlayerOwnerProvider.INSTANCE);
        //register("team",TeamOwnerProvider.INSTANCE);
    }

    private static void register(String name, PotentialOwnerProvider provider) {
        REGISTER.register(name,() -> provider);
    }

}