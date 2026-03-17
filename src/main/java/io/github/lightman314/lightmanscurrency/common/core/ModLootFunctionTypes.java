package io.github.lightman314.lightmanscurrency.common.core;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.loot.functions.ModelVariantLootFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModLootFunctionTypes {

    public static final DeferredRegister<LootItemFunctionType<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.LOOT_FUNCTION_TYPE,LightmansCurrency.MODID);

    public static final Supplier<LootItemFunctionType<ModelVariantLootFunction>> MODEL_VARIANT = register("model_variant",() -> ModelVariantLootFunction.CODEC);

    public static <T extends LootItemFunction> DeferredHolder<LootItemFunctionType<?>,LootItemFunctionType<T>> register(String id,Supplier<MapCodec<T>> codec) {
        return REGISTER.register(id,() -> new LootItemFunctionType<>(codec.get()));
    }

}
