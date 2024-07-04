package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.loot.entries.ConfigLoot;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;

import java.util.function.Supplier;

public class ModLootPoolEntryTypes {

    /**
     * Placeholder function to force the static class loading
     */
    public static void init() {}

    public static final Supplier<LootPoolEntryType> LOOT_TIER_TYPE;

    static {
        LOOT_TIER_TYPE = ModRegistries.LOOT_POOL_ENTRY_TYPES.register("configured_item", () -> new LootPoolEntryType(ConfigLoot.CODEC));
    }

}
