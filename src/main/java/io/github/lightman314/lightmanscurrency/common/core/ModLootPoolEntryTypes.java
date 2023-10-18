package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.loot.entries.ConfigLoot;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraftforge.registries.RegistryObject;

public class ModLootPoolEntryTypes {

    /**
     * Placeholder function to force the static class loading
     */
    public static void init() {}

    public static final RegistryObject<LootPoolEntryType> LOOT_TIER_TYPE;

    static {
        LOOT_TIER_TYPE = ModRegistries.LOOT_POOL_ENTRY_TYPES.register("configured_item", () -> new LootPoolEntryType(ConfigLoot.CODEC));
    }

}
