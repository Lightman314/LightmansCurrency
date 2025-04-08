package io.github.lightman314.lightmanscurrency.integration.quark;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import io.github.lightman314.lightmanscurrency.datagen.util.WoodData;
import io.github.lightman314.lightmanscurrency.datagen.util.WoodDataHelper;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.ForgeRegistries;
import org.violetmoon.quark.base.handler.WoodSetHandler;
import org.violetmoon.quark.content.world.module.AncientWoodModule;
import org.violetmoon.quark.content.world.module.BlossomTreesModule;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class QuarkCustomWoodTypes {

    private static final String MODID = "quark";

    public static final WoodType ANCIENT = WoodType.builder("ancient", MODID).ofColor(MapColor.TERRACOTTA_WHITE).ofName("Ashen").build();
    public static final WoodType AZALEA = WoodType.builder("azalea", MODID).ofColor(MapColor.COLOR_LIGHT_GREEN).ofName("Azalea").build();
    public static final WoodType BLOSSOM = WoodType.builder("blossom", MODID).ofColor(MapColor.COLOR_RED).ofName("Trumpet").build();

    public static void setupWoodTypes() {
        registerSet(ANCIENT, () -> AncientWoodModule.woodSet);
        //registerSet(AZALEA, () -> AzaleaWoodModule.woodSet);
        registerSet(BLOSSOM, () -> BlossomTreesModule.woodSet);
        //Register azalea manually cause apparently they decided to make its wood set private in 1.20...
        WoodDataHelper.register(AZALEA, WoodData.of2(item("azalea_log"),item("azalea_planks"),item("azalea_planks_slab"),"quark:block/azalea_log","quark:block/azalea_log_top","quark:block/azalea_planks"));
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
            ResourceLocation itemID = VersionUtil.modResource(MODID, type.id + "_planks_slab");
            Item result = ForgeRegistries.ITEMS.getValue(itemID);
            if(result == Items.AIR)
                return null;
            LightmansCurrency.LogDebug("Manually found the quark slab for wood type " + type.id + " since quark screwed this up and made their variant handler return the method input instead of the variant that was made...");
            return result;
        };
    }

    private static Supplier<ItemLike> item(@Nonnull String itemID) {
        return () -> {
            Item item = ForgeRegistries.ITEMS.getValue(VersionUtil.modResource(MODID, itemID));
            if(item == Items.AIR)
                return null;
            return item;
        };
    }

}