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

    public static final WoodType LAVAWOOD = WoodType.builder("lavawood",MODID).ofName("Lavawood").ofColor(MaterialColor.COLOR_ORANGE).withAttributes(WoodType.Attributes.PLANKS_AND_SLAB_ONLY).build();
    public static final WoodType BLAZEWOOD = WoodType.builder("blazewood",MODID).ofName("Blazewood").ofColor(MaterialColor.COLOR_ORANGE).withAttributes(WoodType.Attributes.PLANKS_AND_SLAB_ONLY).build();
    public static final WoodType GREENHEART = WoodType.builder("greenheart",MODID).ofName("Greenheart").ofColor(MaterialColor.COLOR_LIGHT_GREEN).build();
    public static final WoodType SKYROOT = WoodType.builder("skyroot",MODID).ofName("Skyroot").ofColor(MaterialColor.COLOR_CYAN).build();
    public static final WoodType BLOODSHROOM = WoodType.builder("bloodshroom",MODID).ofName("Bloodshroom").ofColor(MaterialColor.COLOR_RED).build();
    public static final WoodType ENDERBARK = WoodType.builder("enderbark",MODID).ofName("Enderbark").ofColor(MaterialColor.COLOR_RED).build();

    public static void setupWoodTypes()
    {
        setupType(GREENHEART,"greenheart");
        setupType(SKYROOT,"skyroot");
        setupType(BLOODSHROOM,"bloodshroom");
        setupType(ENDERBARK,"enderbark");

        WoodDataHelper.register(LAVAWOOD, WoodData.of2(null,getSupplier("lavawood",null), getSupplier("lavawood","slab"), null, null, new ResourceLocation(MODID,"block/lavawood")));
        WoodDataHelper.register(BLAZEWOOD, WoodData.of2(null,getSupplier("blazewood",null), getSupplier("blazewood","slab"), null, null, new ResourceLocation(MODID,"block/blazewood")));
    }

    private static void setupType(@Nonnull WoodType type, @Nonnull String name)
    {
        WoodDataHelper.register(type, WoodData.of2(getSupplier(name,"log"), getSupplier(name,"planks"),getSupplier(name,"planks_slab"),new ResourceLocation(MODID,"block/wood/" + name + "/log"),new ResourceLocation(MODID,"block/wood/" + name + "/log_top"),new ResourceLocation(MODID,"block/wood/" + name + "/planks")));
    }

    private static Supplier<? extends ItemLike> getSupplier(@Nonnull String name, @Nullable String type) {
        return WoodDataHelper.supplier(new ResourceLocation(MODID, name + (type != null ? "_" + type : "")));
    }


}
