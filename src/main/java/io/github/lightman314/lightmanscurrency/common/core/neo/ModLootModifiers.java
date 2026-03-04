package io.github.lightman314.lightmanscurrency.common.core.neo;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.variants.block.loot.VariantDataModifier;
import io.github.lightman314.lightmanscurrency.common.loot.glm.*;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModLootModifiers {

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> REGISTER = DeferredRegister.create(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS, LightmansCurrency.MODID);

	public static final Supplier<MapCodec<CoinsInChestsModifier>> COINS_IN_CHESTS = register("coins_in_chests", () -> CoinsInChestsModifier.CODEC);
	public static final Supplier<MapCodec<VariantDataModifier>> MODEL_VARIANT = register("model_variant", () -> VariantDataModifier.CODEC);

    public static <T extends IGlobalLootModifier> DeferredHolder<MapCodec<? extends IGlobalLootModifier>,MapCodec<T>> register(String name,Supplier<MapCodec<T>> factory) {
        return REGISTER.register(name,factory);
    }

}
