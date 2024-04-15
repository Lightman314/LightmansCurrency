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
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.quark.base.handler.WoodSetHandler;
import vazkii.quark.content.building.module.BambooBackportModule;
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
    public static final WoodType BAMBOO = WoodType.builder("bamboo", MODID).ofColor(MaterialColor.COLOR_YELLOW).ofName("Bamboo").build();

    public static void setupWoodTypes() {
        registerSet(ANCIENT, () -> AncientWoodModule.woodSet);
        registerSet(AZALEA, () -> AzaleaWoodModule.woodSet);
        registerSet(BLOSSOM, () -> BlossomTreesModule.woodSet);
        registerBamboo(() -> BambooBackportModule.woodSet);
    }

    private static void registerSet(@Nonnull WoodType type, @Nonnull Supplier<WoodSetHandler.WoodSet> set) {
        WoodDataHelper.register(type, WoodData.of2(log(set), plank(set), slab(type), "quark:block/" + type.id + "_log", "quark:block/" + type.id + "_log_top", "quark:block/" + type.id + "_planks"));
    }

    private static void registerBamboo(@Nonnull Supplier<WoodSetHandler.WoodSet> set) {
        WoodDataHelper.register(BAMBOO, WoodData.of2(WoodDataHelper.supplier(new ResourceLocation(MODID,"bamboo_block")), plank(set), slab(BAMBOO), "quark:block/bamboo_block","quark:block/bamboo_block_top","quark:block/bamboo_planks"));
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

    private static Supplier<? extends ItemLike> slab(@Nonnull WoodType type) { return WoodDataHelper.supplier(new ResourceLocation(MODID,type.id + "_planks_slab")); }

}