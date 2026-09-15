package io.github.lightman314.lightmanscurrency.api.loot;

import com.google.common.collect.Sets;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Collections;
import java.util.Set;

public class LCLootTables {

    private static final Set<ResourceKey<LootTable>> LOCATIONS = Sets.newHashSet();
    private static final Set<ResourceKey<LootTable>> IMMUTABLE_LOCATIONS = Collections.unmodifiableSet(LOCATIONS);

    public static final Identifier ENTITY_DROPS_T1 = register("loot_addons/entity/tier1");
    public static final Identifier ENTITY_DROPS_T2 = register("loot_addons/entity/tier2");
    public static final Identifier ENTITY_DROPS_T3 = register("loot_addons/entity/tier3");
    public static final Identifier ENTITY_DROPS_T4 = register("loot_addons/entity/tier4");
    public static final Identifier ENTITY_DROPS_T5 = register("loot_addons/entity/tier5");
    public static final Identifier ENTITY_DROPS_T6 = register("loot_addons/entity/tier6");
    public static final Identifier BOSS_DROPS_T1 = register("loot_addons/boss/tier1");
    public static final Identifier BOSS_DROPS_T2 = register("loot_addons/boss/tier2");
    public static final Identifier BOSS_DROPS_T3 = register("loot_addons/boss/tier3");
    public static final Identifier BOSS_DROPS_T4 = register("loot_addons/boss/tier4");
    public static final Identifier BOSS_DROPS_T5 = register("loot_addons/boss/tier5");
    public static final Identifier BOSS_DROPS_T6 = register("loot_addons/boss/tier6");

    public static final Identifier CHEST_DROPS_T1 = register("loot_addons/chest/tier1");
    public static final Identifier CHEST_DROPS_T2 = register("loot_addons/chest/tier2");
    public static final Identifier CHEST_DROPS_T3 = register("loot_addons/chest/tier3");
    public static final Identifier CHEST_DROPS_T4 = register("loot_addons/chest/tier4");
    public static final Identifier CHEST_DROPS_T5 = register("loot_addons/chest/tier5");
    public static final Identifier CHEST_DROPS_T6 = register("loot_addons/chest/tier6");

    public static final Identifier ARCHAEOLOGY_VILLAGE_DESERT_BANKER = register("archaeology/village/desert_banker");
    public static final Identifier ARCHAEOLOGY_VILLAGE_PLAINS_SHOP = register("archaeology/village/plains_shop");
    public static final Identifier ARCHAEOLOGY_VILLAGE_TAIGA_SHOP = register("archaeology/village/taiga_shop");
    public static final Identifier ARCHAEOLOGY_VILLAGE_DESERT_SHOP = register("archaeology/village/desert_shop");

    public static final Identifier ARCHAEOLOGY_ANCIENT_RUINS = register("archaeology/ancient_city/ancient_ruins");

    private static Identifier register(String id) { return register(LCApi.id( id)); }

    private static Identifier register(Identifier id) {
        if (LOCATIONS.add(ResourceKey.create(Registries.LOOT_TABLE,id))) {
            return id;
        } else {
            throw new IllegalArgumentException(id + " is already a registered LightmansCurrency loot table");
        }
    }

    public static Set<ResourceKey<LootTable>> all() { return IMMUTABLE_LOCATIONS; }

}