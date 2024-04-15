package io.github.lightman314.lightmanscurrency.datagen.util;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class WoodDataHelper {

    private static final Map<WoodType,WoodData> registeredData;


    static {
        registeredData = new HashMap<>();

        //Register Vanilla Types
        register(WoodType.OAK, WoodData.of(Items.OAK_LOG, Items.OAK_PLANKS, Items.OAK_SLAB, "minecraft:block/oak_log", "minecraft:block/oak_log_top", "minecraft:block/oak_planks"));
        register(WoodType.SPRUCE, WoodData.of(Items.SPRUCE_LOG, Items.SPRUCE_PLANKS, Items.SPRUCE_SLAB, "minecraft:block/spruce_log", "minecraft:block/spruce_log_top", "minecraft:block/spruce_planks"));
        register(WoodType.BIRCH, WoodData.of(Items.BIRCH_LOG, Items.BIRCH_PLANKS, Items.BIRCH_SLAB, "minecraft:block/birch_log", "minecraft:block/birch_log_top", "minecraft:block/birch_planks"));
        register(WoodType.JUNGLE, WoodData.of(Items.JUNGLE_LOG, Items.JUNGLE_PLANKS, Items.JUNGLE_SLAB, "minecraft:block/jungle_log", "minecraft:block/jungle_log_top", "minecraft:block/jungle_planks"));
        register(WoodType.ACACIA, WoodData.of(Items.ACACIA_LOG, Items.ACACIA_PLANKS, Items.ACACIA_SLAB, "minecraft:block/acacia_log", "minecraft:block/acacia_log_top", "minecraft:block/acacia_planks"));
        register(WoodType.DARK_OAK, WoodData.of(Items.DARK_OAK_LOG, Items.DARK_OAK_PLANKS, Items.DARK_OAK_SLAB, "minecraft:block/dark_oak_log", "minecraft:block/dark_oak_log_top", "minecraft:block/dark_oak_planks"));
        register(WoodType.MANGROVE, WoodData.of(Items.MANGROVE_LOG, Items.MANGROVE_PLANKS, Items.MANGROVE_SLAB, "minecraft:block/mangrove_log", "minecraft:block/mangrove_log_top", "minecraft:block/mangrove_planks"));
        register(WoodType.CRIMSON, WoodData.of(Items.CRIMSON_STEM, Items.CRIMSON_PLANKS, Items.CRIMSON_SLAB, "minecraft:block/crimson_stem", "minecraft:block/crimson_stem_top", "minecraft:block/crimson_planks"));
        register(WoodType.WARPED, WoodData.of(Items.WARPED_STEM, Items.WARPED_PLANKS, Items.WARPED_SLAB, "minecraft:block/warped_stem", "minecraft:block/warped_stem_top", "minecraft:block/warped_planks"));

    }

    public static void register(@Nonnull WoodType type, @Nonnull WoodData data) {
        if(registeredData.containsKey(type))
        {
            LightmansCurrency.LogError("Attempted to register a duplicate WoodData for type '" + type.id + "'!");
            return;
        }
        registeredData.put(type, Objects.requireNonNull(data));
    }

    @Nullable
    public static WoodData get(WoodType type) { return registeredData.get(type); }

    public static Supplier<? extends ItemLike> supplier(@Nonnull ResourceLocation itemID)
    {
        return () -> {
            ItemLike result = ForgeRegistries.ITEMS.getValue(itemID);
            if(result == Items.AIR)
            {
                LightmansCurrency.LogError("Could not find " + itemID);
                return null;
            }
            return result;
        };
    }

}