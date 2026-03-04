package io.github.lightman314.lightmanscurrency.common.core.neo;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.holder_sets.ItemListOptionSet;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.holdersets.HolderSetType;

public class ModHolderSets {

    public static final DeferredRegister<HolderSetType> REGISTER = DeferredRegister.create(NeoForgeRegistries.HOLDER_SET_TYPES,LightmansCurrency.MODID);

    static {
        REGISTER.register("item_list_config", () -> ItemListOptionSet.TYPE);
    }

}
