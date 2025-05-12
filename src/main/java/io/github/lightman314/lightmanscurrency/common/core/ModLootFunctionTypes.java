package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.loot.functions.ModelVariantLootFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

import java.util.function.Supplier;

public class ModLootFunctionTypes {

    public static void init() {}

    public static final Supplier<LootItemFunctionType> MODEL_VARIANT;

    static {
        MODEL_VARIANT = ModRegistries.LOOT_ITEM_FUNCTION_TYPES.register("model_variant",() -> new LootItemFunctionType(ModelVariantLootFunction.CODEC));
    }
}