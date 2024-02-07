package io.github.lightman314.lightmanscurrency.integration.quark;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import io.github.lightman314.lightmanscurrency.datagen.util.WoodData;
import io.github.lightman314.lightmanscurrency.datagen.util.WoodDataHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.quark.base.handler.WoodSetHandler;
import vazkii.quark.content.world.module.AncientWoodModule;
import vazkii.quark.content.world.module.AzaleaWoodModule;
import vazkii.quark.content.world.module.BlossomTreesModule;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class QuarkCustomWoodTypes {

    private static final String MODID = "quark";

    public static final WoodType ANCIENT = WoodType.builder("ancient", MODID).ofColor(MaterialColor.TERRACOTTA_WHITE).ofName("Ancient").build();
    public static final WoodType AZALEA = WoodType.builder("azalea", MODID).ofColor(MaterialColor.COLOR_LIGHT_GREEN).ofName("Azalea").build();
    public static final WoodType BLOSSOM = WoodType.builder("blossom", MODID).ofColor(MaterialColor.COLOR_RED).ofName("Blossom").build();

    public static void setupWoodTypes() {
        registerSet(ANCIENT, () -> AncientWoodModule.woodSet);
        registerSet(AZALEA, () -> AzaleaWoodModule.woodSet);
        registerSet(BLOSSOM, () -> BlossomTreesModule.woodSet);
    }

    private static void registerSet(@Nonnull WoodType type, @Nonnull Supplier<WoodSetHandler.WoodSet> set) {
        WoodDataHelper.register(type, WoodData.of2(log(set), plank(set), slab(type), "quark:block/" + type.id + "_log", "quark:block/" + type.id + "_log_top", "quark:block/" + type.id + "_planks"));
    }

    private static Supplier<ItemLike> log(@Nonnull Supplier<WoodSetHandler.WoodSet> set) {
        return () -> {
            WoodSetHandler.WoodSet s = set.get();
            if(s == null)
                return null;
            return s.log;
        };
    }

    private static Supplier<ItemLike> plank(@Nonnull Supplier<WoodSetHandler.WoodSet> set) {
        return () -> {
            WoodSetHandler.WoodSet s = set.get();
            if(s == null)
                return null;
            return s.planks;
        };
    }

    private static Supplier<ItemLike> slab(@Nonnull WoodType type) {
        return () -> {
            //Manually get slab block cause quark screwed this part up
            ResourceLocation itemID = new ResourceLocation(MODID, type.id + "_planks_slab");
            Item result = ForgeRegistries.ITEMS.getValue(itemID);
            if(result == Items.AIR)
                return null;
            LightmansCurrency.LogDebug("Manually found the quark slab for wood type " + type.id + " since quark screwed this up and made their variant handler return the method input instead of the variant that was made...");
            return result;
        };
    }

}