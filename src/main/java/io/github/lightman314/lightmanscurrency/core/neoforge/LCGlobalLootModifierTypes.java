package io.github.lightman314.lightmanscurrency.core.neoforge;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.features.loot.ConfiguredCoinLootModifier;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class LCGlobalLootModifierTypes {
    private LCGlobalLootModifierTypes() {}

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> REGISTER = DeferredRegister.create(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS,LCApi.MODID);

    static {
        register("configured_coin_loot",ConfiguredCoinLootModifier.CODEC);
    }

    private static void register(String name,MapCodec<? extends IGlobalLootModifier> codec) {
        REGISTER.register(name,() -> codec);
    }

}