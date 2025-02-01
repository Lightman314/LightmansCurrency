package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.loot.entries.*;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraftforge.registries.RegistryObject;

public class ModLootPoolEntryTypes {

    /**
     * Placeholder function to force the static class loading
     */
    public static void init() {}

    public static final RegistryObject<LootPoolEntryType> LOOT_TIER_TYPE;
    public static final RegistryObject<LootPoolEntryType> ANCIENT_COIN_TYPE;
    public static final RegistryObject<LootPoolEntryType> VANILLA_LOOT_TABLE;

    static {
        LOOT_TIER_TYPE = ModRegistries.LOOT_POOL_ENTRY_TYPES.register("configured_item", () -> new LootPoolEntryType(new ConfigLoot.Serializer()));
        ANCIENT_COIN_TYPE = ModRegistries.LOOT_POOL_ENTRY_TYPES.register("ancient_coin", () -> new LootPoolEntryType(new AncientCoinLoot.Serializer()));
        VANILLA_LOOT_TABLE = ModRegistries.LOOT_POOL_ENTRY_TYPES.register("vanilla_loot_table", () -> new LootPoolEntryType(new VanillaLootTableReference.Serializer()));
    }

}
