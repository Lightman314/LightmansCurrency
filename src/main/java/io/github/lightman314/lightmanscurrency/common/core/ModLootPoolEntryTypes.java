package io.github.lightman314.lightmanscurrency.common.core;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.loot.entries.AncientCoinLoot;
import io.github.lightman314.lightmanscurrency.common.loot.entries.ConfigLoot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModLootPoolEntryTypes {

    public static final DeferredRegister<LootPoolEntryType> REGISTER = DeferredRegister.create(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE,LightmansCurrency.MODID);

    public static final Supplier<LootPoolEntryType> LOOT_TIER_TYPE = register("configured_item",() -> ConfigLoot.CODEC);
    public static final Supplier<LootPoolEntryType> ANCIENT_COIN_TYPE = register("ancient_coin",() -> AncientCoinLoot.CODEC);

    public static DeferredHolder<LootPoolEntryType,LootPoolEntryType> register(String id, Supplier<MapCodec<? extends LootPoolEntryContainer>> codec) {
        return REGISTER.register(id,() -> new LootPoolEntryType(codec.get()));
    }

}
