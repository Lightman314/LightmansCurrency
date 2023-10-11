package io.github.lightman314.lightmanscurrency.common.loot;

import com.google.common.collect.Sets;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.Set;

public class LCLootTables {

    private static final Set<ResourceLocation> LOCATIONS = Sets.newHashSet();
    private static final Set<ResourceLocation> IMMUTABLE_LOCATIONS = Collections.unmodifiableSet(LOCATIONS);

    public static final ResourceLocation ENTITY_DROPS_T1 = register("loot_addons/entity/tier1");
    public static final ResourceLocation ENTITY_DROPS_T2 = register("loot_addons/entity/tier2");
    public static final ResourceLocation ENTITY_DROPS_T3 = register("loot_addons/entity/tier3");
    public static final ResourceLocation ENTITY_DROPS_T4 = register("loot_addons/entity/tier4");
    public static final ResourceLocation ENTITY_DROPS_T5 = register("loot_addons/entity/tier5");
    public static final ResourceLocation ENTITY_DROPS_T6 = register("loot_addons/entity/tier6");
    public static final ResourceLocation BOSS_DROPS_T1 = register("loot_addons/boss/tier1");
    public static final ResourceLocation BOSS_DROPS_T2 = register("loot_addons/boss/tier2");
    public static final ResourceLocation BOSS_DROPS_T3 = register("loot_addons/boss/tier3");
    public static final ResourceLocation BOSS_DROPS_T4 = register("loot_addons/boss/tier4");
    public static final ResourceLocation BOSS_DROPS_T5 = register("loot_addons/boss/tier5");
    public static final ResourceLocation BOSS_DROPS_T6 = register("loot_addons/boss/tier6");

    public static final ResourceLocation CHEST_DROPS_T1 = register("loot_addons/chest/tier1");
    public static final ResourceLocation CHEST_DROPS_T2 = register("loot_addons/chest/tier2");
    public static final ResourceLocation CHEST_DROPS_T3 = register("loot_addons/chest/tier3");
    public static final ResourceLocation CHEST_DROPS_T4 = register("loot_addons/chest/tier4");
    public static final ResourceLocation CHEST_DROPS_T5 = register("loot_addons/chest/tier5");
    public static final ResourceLocation CHEST_DROPS_T6 = register("loot_addons/chest/tier6");

    private static ResourceLocation register(String id) { return register(new ResourceLocation(LightmansCurrency.MODID, id)); }

    private static ResourceLocation register(ResourceLocation id) {
        if (LOCATIONS.add(id)) {
            return id;
        } else {
            throw new IllegalArgumentException(id + " is already a registered LightmansCurrency loot table");
        }
    }

    public static Set<ResourceLocation> all() { return IMMUTABLE_LOCATIONS; }

}
