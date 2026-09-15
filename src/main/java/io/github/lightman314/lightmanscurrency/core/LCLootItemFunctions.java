package io.github.lightman314.lightmanscurrency.core;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.features.loot.functions.RandomizeItemFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCLootItemFunctions {
    private LCLootItemFunctions() {}

    public static final DeferredRegister<MapCodec<? extends LootItemFunction>> REGISTER = DeferredRegister.create(BuiltInRegistries.LOOT_FUNCTION_TYPE,LCApi.MODID);

    static {
        register("randomized_item",RandomizeItemFunction.MAP_CODEC);
    }

    private static void register(String name, MapCodec<? extends LootItemFunction> codec) {
        REGISTER.register(name,() -> codec);
    }

}
