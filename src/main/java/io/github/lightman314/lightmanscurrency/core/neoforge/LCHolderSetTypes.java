package io.github.lightman314.lightmanscurrency.core.neoforge;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.coins.holders.CoinHolderSet;
import io.github.lightman314.lightmanscurrency.api.config.data.ItemListOptionSet;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.holdersets.HolderSetType;

public final class LCHolderSetTypes {
    private LCHolderSetTypes() {}

    public static final DeferredRegister<HolderSetType> REGISTER = DeferredRegister.create(NeoForgeRegistries.HOLDER_SET_TYPES,LCApi.MODID);

    static {
        register("item_list_config",ItemListOptionSet.TYPE);
        register("coin",CoinHolderSet.TYPE);
    }

    private static void register(String name,HolderSetType type) {
        REGISTER.register(name,() -> type);
    }

}
