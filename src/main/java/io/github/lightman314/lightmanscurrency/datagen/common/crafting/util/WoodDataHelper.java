package io.github.lightman314.lightmanscurrency.datagen.common.crafting.util;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import net.minecraft.world.item.Items;

public class WoodDataHelper {

    public static WoodData forVanillaType(String name) {
        WoodType type = WoodType.fromTypeName(name);
        if(type != null)
        {
            if(type == WoodType.OAK)
                return WoodData.of(Items.OAK_LOG, Items.OAK_PLANKS, Items.OAK_SLAB, "minecraft:block/oak_log", "minecraft:block/oak_log_top", "minecraft:block/oak_planks");
            if(type == WoodType.SPRUCE)
                return WoodData.of(Items.SPRUCE_LOG, Items.SPRUCE_PLANKS, Items.SPRUCE_SLAB, "minecraft:block/spruce_log", "minecraft:block/spruce_log_top", "minecraft:block/spruce_planks");
            if(type == WoodType.BIRCH)
                return WoodData.of(Items.BIRCH_LOG, Items.BIRCH_PLANKS, Items.BIRCH_SLAB, "minecraft:block/birch_log", "minecraft:block/birch_log_top", "minecraft:block/birch_planks");
            if(type == WoodType.JUNGLE)
                return WoodData.of(Items.JUNGLE_LOG, Items.JUNGLE_PLANKS, Items.JUNGLE_SLAB, "minecraft:block/jungle_log", "minecraft:block/jungle_log_top", "minecraft:block/jungle_planks");
            if(type == WoodType.ACACIA)
                return WoodData.of(Items.ACACIA_LOG, Items.ACACIA_PLANKS, Items.ACACIA_SLAB, "minecraft:block/acacia_log", "minecraft:block/acacia_log_top", "minecraft:block/acacia_planks");
            if(type == WoodType.DARK_OAK)
                return WoodData.of(Items.DARK_OAK_LOG, Items.DARK_OAK_PLANKS, Items.DARK_OAK_SLAB, "minecraft:block/dark_oak_log", "minecraft:block/dark_oak_log_top", "minecraft:block/dark_oak_planks");
            if(type == WoodType.MANGROVE)
                return WoodData.of(Items.MANGROVE_LOG, Items.MANGROVE_PLANKS, Items.MANGROVE_SLAB, "minecraft:block/mangrove_log", "minecraft:block/mangrove_log_top", "minecraft:block/mangrove_planks");
            if(type == WoodType.CHERRY)
                return WoodData.of(Items.CHERRY_LOG, Items.CHERRY_PLANKS, Items.CHERRY_SLAB, "minecraft:block/cherry_log", "minecraft:block/cherry_log_top", "minecraft:block/cherry_planks");
            if(type == WoodType.BAMBOO)
                return WoodData.of(Items.BAMBOO_BLOCK, Items.BAMBOO_PLANKS, Items.BAMBOO_SLAB, "minecraft:block/bamboo_block", "minecraft:block/bamboo_block_top", "minecraft:block/bamboo_planks");
            if(type == WoodType.CRIMSON)
                return WoodData.of(Items.CRIMSON_STEM, Items.CRIMSON_PLANKS, Items.CRIMSON_SLAB, "minecraft:block/crimson_stem", "minecraft:block/crimson_stem_top", "minecraft:block/crimson_planks");
            if(type == WoodType.WARPED)
                return WoodData.of(Items.WARPED_STEM, Items.WARPED_PLANKS, Items.WARPED_SLAB, "minecraft:block/warped_stem", "minecraft:block/warped_stem_top", "minecraft:block/warped_planks");
            LightmansCurrency.LogDebug("Wood Type '" + name + "' could not be mapped to a vanilla Wood Type!");
            return null;
        }
        LightmansCurrency.LogDebug("Wood Type '" + name + "' could not be mapped to a valid Wood Type!");
        return null;
    }
}
