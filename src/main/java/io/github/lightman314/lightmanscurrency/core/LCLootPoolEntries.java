package io.github.lightman314.lightmanscurrency.core;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.loot.pool.ConfigLoot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCLootPoolEntries {
    private LCLootPoolEntries() {}

    public static final DeferredRegister<MapCodec<? extends LootPoolEntryContainer>> REGISTER = DeferredRegister.create(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, LCApi.MODID);

    static {
        REGISTER.register("tiered_loot",() -> ConfigLoot.CODEC);
    }

}