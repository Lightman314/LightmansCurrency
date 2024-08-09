package io.github.lightman314.lightmanscurrency.integration.tconstruct;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import io.github.lightman314.lightmanscurrency.datagen.util.WoodData;
import io.github.lightman314.lightmanscurrency.datagen.util.WoodDataHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class TinkersCustomWoodTypes {

    private static final String MODID = "tconstruct";


    public static final WoodType GREENHEART = WoodType.builder("greenheart",MODID).ofName("Greenheart").ofColor(MaterialColor.COLOR_LIGHT_GREEN).build();
    public static final WoodType SKYROOT = WoodType.builder("skyroot",MODID).ofName("Skyroot").ofColor(MaterialColor.COLOR_CYAN).build();
    public static final WoodType BLOODSHROOM = WoodType.builder("bloodshroom",MODID).ofName("Bloodshroom").ofColor(MaterialColor.COLOR_RED).build();
    public static final WoodType ENDERBARK = WoodType.builder("enderbark",MODID).ofName("Enderbark").ofColor(MaterialColor.COLOR_RED).build();

    public static final WoodType LAVAWOOD = WoodType.builder("lavawood",MODID).ofName("Lavawood").ofColor(MaterialColor.COLOR_ORANGE).withAttributes(WoodType.Attributes.PLANKS_AND_SLAB_ONLY).build();
    public static final WoodType BLAZEWOOD = WoodType.builder("blazewood",MODID).ofName("Blazewood").ofColor(MaterialColor.COLOR_ORANGE).withAttributes(WoodType.Attributes.PLANKS_AND_SLAB_ONLY).build();
    public static final WoodType NAHUATL = WoodType.builder("nahuatl",MODID).ofName("Nahuatl").ofColor(MaterialColor.COLOR_BLACK).withAttributes(WoodType.Attributes.PLANKS_AND_SLAB_ONLY).build();

    public static void setupWoodTypes()
    {
        setupType(GREENHEART);
        setupType(SKYROOT);
        setupType(BLOODSHROOM);
        setupType(ENDERBARK);

        setupSimpleType(LAVAWOOD);
        setupSimpleType(BLAZEWOOD);
        //Manually do nahuatl because it's plank texture is in a weird location
        WoodDataHelper.register(NAHUATL, WoodData.of2(null,getSupplier("nahuatl",null), getSupplier("nahuatl","slab"), null, null, new ResourceLocation(MODID,"block/wood/nahuatl")));
    }

    private static void setupType(@Nonnull WoodType type)
    {
        WoodDataHelper.register(type, WoodData.of2(getSupplier(type.id,"log"), getSupplier(type.id,"planks"),getSupplier(type.id,"planks_slab"),new ResourceLocation(MODID,"block/wood/" + type.id + "/log"),new ResourceLocation(MODID,"block/wood/" + type.id + "/log_top"),new ResourceLocation(MODID,"block/wood/" + type.id + "/planks")));
    }

    private static void setupSimpleType(@Nonnull WoodType type)
    {
        WoodDataHelper.register(type, WoodData.of2(null, getSupplier(type.id,null), getSupplier(type.id,"slab"),null,null, new ResourceLocation(MODID,"block/wood/" + type.id)));
    }

    private static Supplier<? extends ItemLike> getSupplier(@Nonnull String name, @Nullable String type) {
        return WoodDataHelper.supplier(new ResourceLocation(MODID, name + (type != null ? "_" + type : "")));
    }


}
