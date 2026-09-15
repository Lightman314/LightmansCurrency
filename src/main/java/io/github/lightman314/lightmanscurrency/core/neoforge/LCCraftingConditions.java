package io.github.lightman314.lightmanscurrency.core.neoforge;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.config.data.ConfigCraftingCondition;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class LCCraftingConditions {
    private LCCraftingConditions() {}

    public static final DeferredRegister<MapCodec<? extends ICondition>> REGISTER = DeferredRegister.create(NeoForgeRegistries.CONDITION_SERIALIZERS, LCApi.MODID);

    static {
        register("configured",ConfigCraftingCondition.CODEC);
    }

    private static void register(String name,MapCodec<? extends ICondition> codec) {
        REGISTER.register(name,() -> codec);
    }

}
