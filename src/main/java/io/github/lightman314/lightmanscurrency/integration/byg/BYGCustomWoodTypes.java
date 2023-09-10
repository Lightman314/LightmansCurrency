package io.github.lightman314.lightmanscurrency.integration.byg;

import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType.Attributes;
import io.github.lightman314.lightmanscurrency.datagen.util.WoodData;
import io.github.lightman314.lightmanscurrency.datagen.util.WoodDataHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.MaterialColor;
import potionstudios.byg.common.block.BYGBlocks;
import potionstudios.byg.reg.BlockRegistryObject;

import java.util.function.Supplier;

public class BYGCustomWoodTypes {

    private static final String MODID = "byg";

    public static final WoodType ASPEN = WoodType.builder("aspen", MODID).ofName("Aspen").ofColor(MaterialColor.TERRACOTTA_YELLOW).build();
    public static final WoodType BAOBAB = WoodType.builder("baobab", MODID).ofName("Baobab").ofColor(MaterialColor.TERRACOTTA_GREEN).build();
    //public static final WoodType IMBUED_BLUE_ENCHANTED = WoodType.builder("imbued_blue_enchanted", MODID).ofName("Imbued Blue Enchanted").ofColor(MaterialColor.COLOR_BLUE).withAttributes(Attributes.LOG_ONLY).build();
    public static final WoodType BLUE_ENCHANTED = WoodType.builder("blue_enchanted", MODID).ofName("Blue Enchanted").ofColor(MaterialColor.COLOR_BLUE).build();
    public static final WoodType BULBIS = WoodType.builder("bulbis", MODID).ofName("Bulbis").ofColor(MaterialColor.COLOR_BLUE).build();
    public static final WoodType CHERRY = WoodType.builder("cherry", MODID).ofName("Cherry").ofColor(MaterialColor.WOOD).build();
    public static final WoodType CIKA = WoodType.builder("cika", MODID).ofName("Cika").ofColor(MaterialColor.TERRACOTTA_ORANGE).build();
    public static final WoodType CYPRESS = WoodType.builder("cypress", MODID).ofName("Cypress").ofColor(MaterialColor.TERRACOTTA_LIGHT_GREEN).build();
    public static final WoodType EBONY = WoodType.builder("ebony", MODID).ofName("Ebony").ofColor(MaterialColor.COLOR_GREEN).build();
    public static final WoodType ETHER = WoodType.builder("ether", MODID).ofName("Ether").ofColor(MaterialColor.COLOR_CYAN).build();
    public static final WoodType FIR = WoodType.builder("fir", MODID).ofName("Fir").ofColor(MaterialColor.COLOR_GREEN).build();
    //public static final WoodType IMBUED_GREEN_ENCHANTED = WoodType.builder("imbued_green_enchanted", MODID).ofName("Imbued Green Enchanted").ofColor(MaterialColor.COLOR_GREEN).withAttributes(Attributes.LOG_ONLY).build();
    public static final WoodType GREEN_ENCHANTED = WoodType.builder("green_enchanted", MODID).ofName("Green Enchanted").ofColor(MaterialColor.COLOR_GREEN).build();
    public static final WoodType HOLLY = WoodType.builder("holly", MODID).ofName("Holly").ofColor(MaterialColor.TERRACOTTA_GREEN).build();
    public static final WoodType FUNGAL_IMPARIUS = WoodType.builder("fungal_imparius", MODID).ofName("Fungal Imparius").ofColor(MaterialColor.COLOR_BLUE).withAttributes(Attributes.LOG_ONLY).build();
    public static final WoodType IMPARIUS = WoodType.builder("imparius", MODID).ofName("Imparius").ofColor(MaterialColor.COLOR_BLUE).build();
    public static final WoodType LAMENT = WoodType.builder("lament", MODID).ofName("Lament").ofColor(MaterialColor.COLOR_MAGENTA).build();
    public static final WoodType JACARANDA = WoodType.builder("jacaranda", MODID).ofName("Jacaranda").ofColor(MaterialColor.TERRACOTTA_PURPLE).build();
    public static final WoodType MAHOGANY = WoodType.builder("mahogany", MODID).ofName("Mahogany").ofColor(MaterialColor.COLOR_GREEN).build();
    public static final WoodType MANGROVE = WoodType.builder("mangrove", MODID).ofName("Mangrove").ofColor(MaterialColor.COLOR_GREEN).build();
    public static final WoodType MAPLE = WoodType.builder("maple", MODID).ofName("Maple").ofColor(MaterialColor.COLOR_GREEN).build();
    public static final WoodType IMBUED_NIGHTSHADE = WoodType.builder("imbued_nightshade", MODID).ofName("Imbued Nightshade").ofColor(MaterialColor.COLOR_ORANGE).withAttributes(Attributes.LOG_ONLY).build();
    public static final WoodType NIGHTSHADE = WoodType.builder("nightshade", MODID).ofName("Nightshade").ofColor(MaterialColor.COLOR_ORANGE).build();
    public static final WoodType PALM = WoodType.builder("palm", MODID).ofName("Palm").ofColor(MaterialColor.COLOR_GREEN).build();
    public static final WoodType PALO_VERDE = WoodType.builder("palo_verde", MODID).ofName( "Palo Verde").ofColor(MaterialColor.COLOR_GREEN).withAttributes(Attributes.LOG_ONLY).build(); //TODO think of an appropriate color
    public static final WoodType PINE = WoodType.builder("pine", MODID).ofName("Pine").ofColor(MaterialColor.TERRACOTTA_GREEN).build();
    public static final WoodType RAINBOW_EUCALYPTUS = WoodType.builder("rainbow_eucalyptus", MODID).ofName("Rainbow Eucalyptus").ofColor(MaterialColor.COLOR_GREEN).build();
    public static final WoodType REDWOOD = WoodType.builder("redwood", MODID).ofName("Redwood").ofColor(MaterialColor.COLOR_GREEN).build();
    public static final WoodType SKYRIS = WoodType.builder("skyris", MODID).ofName("Skyris").ofColor(MaterialColor.COLOR_PINK).build();
    public static final WoodType WILLOW = WoodType.builder("willow", MODID).ofName("Willow").ofColor(MaterialColor.TERRACOTTA_GREEN).build();
    public static final WoodType WITCH_HAZEL = WoodType.builder("witch_hazel", MODID).ofName( "Witch Hazel").ofColor(MaterialColor.COLOR_ORANGE).build();
    public static final WoodType ZELKOVA = WoodType.builder("zelkova", MODID).ofName("Zelkova").ofColor(MaterialColor.TERRACOTTA_RED).build();
    public static final WoodType SYTHIAN = WoodType.builder("sythian", MODID).ofName("Sythian").ofColor(MaterialColor.WOOD).build(); //TODO think of an appropriate color
    public static final WoodType EMBUR = WoodType.builder("embur", MODID).ofName("Embur").ofColor(MaterialColor.WOOD).build();  //TODO think of an appropriate color
    public static final WoodType WITHERING_OAK = WoodType.builder("withering_oak", MODID).ofName("Withering Oak").ofColor(MaterialColor.WOOD).withAttributes(Attributes.LOG_ONLY).build(); //TODO think of an appropriate color

    public static void setupWoodTypes()
    {
        setupWoodType(ASPEN, () -> BYGBlocks.ASPEN_LOG, () -> BYGBlocks.ASPEN_PLANKS, () -> BYGBlocks.ASPEN_SLAB, "byg:block/aspen_log", "byg:block/aspen_log_top", "byg:block/aspen_planks");
        setupWoodType(BAOBAB, () -> BYGBlocks.BAOBAB_LOG, () -> BYGBlocks.BAOBAB_PLANKS, () -> BYGBlocks.BAOBAB_SLAB, "byg:block/baobab_log", "byg:block/baobab_log_top", "byg:block/baobab_planks");
        //setupWoodType(IMBUED_BLUE_ENCHANTED, () -> BYGBlocks.IMBUED_BLUE_ENCHANTED_LOG, () -> BYGBlocks.BLUE_ENCHANTED_PLANKS, () -> BYGBlocks.BLUE_ENCHANTED_SLAB, "byg:block/blue_enchanted_log2", "byg:block/blue_enchanted_log_top", "byg:block/blue_enchanted_planks");
        setupWoodType(BLUE_ENCHANTED, () -> BYGBlocks.BLUE_ENCHANTED_LOG, () -> BYGBlocks.BLUE_ENCHANTED_PLANKS, () -> BYGBlocks.BLUE_ENCHANTED_SLAB, "byg:block/blue_enchanted_log", "byg:block/blue_enchanted_log_top", "byg:block/blue_enchanted_planks");
        setupWoodType(BULBIS, () -> BYGBlocks.BULBIS_STEM, () -> BYGBlocks.BULBIS_PLANKS, () -> BYGBlocks.BULBIS_SLAB, "byg:block/bulbis_stem", "byg:block/bulbis_stem_top", "byg:block/bulbis_planks");
        setupWoodType(CHERRY, () -> BYGBlocks.CHERRY_LOG, () -> BYGBlocks.CHERRY_PLANKS, () -> BYGBlocks.CHERRY_SLAB, "byg:block/cherry_log", "byg:block/cherry_log_top", "byg:block/cherry_planks");
        setupWoodType(CIKA, () -> BYGBlocks.CIKA_LOG, () -> BYGBlocks.CIKA_PLANKS, () -> BYGBlocks.CIKA_SLAB, "byg:block/cika_log", "byg:block/cika_log_top", "byg:block/cika_planks");
        setupWoodType(CYPRESS, () -> BYGBlocks.CYPRESS_LOG, () -> BYGBlocks.CYPRESS_PLANKS, () -> BYGBlocks.CYPRESS_SLAB, "byg:block/cypress_log", "byg:block/cypress_log_top", "byg:block/cypress_planks");
        setupWoodType(EBONY, () -> BYGBlocks.EBONY_LOG, () -> BYGBlocks.EBONY_PLANKS, () -> BYGBlocks.EBONY_SLAB, "byg:block/ebony_log", "byg:block/ebony_log_top", "byg:block/ebony_planks");
        setupWoodType(ETHER, () -> BYGBlocks.ETHER_LOG, () -> BYGBlocks.ETHER_PLANKS, () -> BYGBlocks.ETHER_SLAB, "byg:block/ether_log", "byg:block/ether_log_top", "byg:block/ether_planks");
        setupWoodType(FIR, () -> BYGBlocks.FIR_LOG, () -> BYGBlocks.FIR_PLANKS, () -> BYGBlocks.FIR_SLAB, "byg:block/fir_log", "byg:block/fir_log_top", "byg:block/fir_planks");
        //setupWoodType(IMBUED_GREEN_ENCHANTED, () -> BYGBlocks.IMBUED_GREEN_ENCHANTED_LOG, () -> BYGBlocks.GREEN_ENCHANTED_PLANKS, () -> BYGBlocks.GREEN_ENCHANTED_SLAB, "byg:block/green_enchanted_log2", "byg:block/green_enchanted_log_top", "byg:block/green_enchanted_planks");
        setupWoodType(GREEN_ENCHANTED, () -> BYGBlocks.GREEN_ENCHANTED_LOG, () -> BYGBlocks.GREEN_ENCHANTED_PLANKS, () -> BYGBlocks.GREEN_ENCHANTED_SLAB, "byg:block/green_enchanted_log", "byg:block/green_enchanted_log_top", "byg:block/green_enchanted_planks");
        setupWoodType(HOLLY, () -> BYGBlocks.HOLLY_LOG, () -> BYGBlocks.HOLLY_PLANKS, () -> BYGBlocks.HOLLY_SLAB, "byg:block/holly_log", "byg:block/holly_log_top", "byg:block/holly_planks");
        setupWoodType(FUNGAL_IMPARIUS, () -> BYGBlocks.FUNGAL_IMPARIUS_STEM, () -> BYGBlocks.IMPARIUS_PLANKS, () -> BYGBlocks.IMPARIUS_SLAB, "byg:block/fungal_imparius_stem", "byg:block/fungal_imparius_stem_top", "byg:block/imparius_planks");
        setupWoodType(IMPARIUS, () -> BYGBlocks.IMPARIUS_STEM, () -> BYGBlocks.IMPARIUS_PLANKS, () -> BYGBlocks.IMPARIUS_SLAB, "byg:block/imparius_stem", "byg:block/imparius_stem_top", "byg:block/imparius_planks");
        setupWoodType(LAMENT, () -> BYGBlocks.LAMENT_LOG, () -> BYGBlocks.LAMENT_PLANKS, () -> BYGBlocks.LAMENT_SLAB, "byg:block/lament_log", "byg:block/lament_log_top", "byg:block/lament_planks");
        setupWoodType(JACARANDA, () -> BYGBlocks.JACARANDA_LOG, () -> BYGBlocks.JACARANDA_PLANKS, () -> BYGBlocks.JACARANDA_SLAB, "byg:block/jacaranda_log", "byg:block/jacaranda_log_top", "byg:block/jacaranda_planks");
        setupWoodType(MAHOGANY, () -> BYGBlocks.MAHOGANY_LOG, () -> BYGBlocks.MAHOGANY_PLANKS, () -> BYGBlocks.MAHOGANY_SLAB, "byg:block/mahogany_log", "byg:block/mahogany_log_top", "byg:block/mahogany_planks");
        setupWoodType(MANGROVE, () -> BYGBlocks.MANGROVE_LOG, () -> BYGBlocks.MANGROVE_PLANKS, () -> BYGBlocks.MANGROVE_SLAB, "byg:block/mangrove_log", "byg:block/mangrove_log_top", "byg:block/mangrove_planks");
        setupWoodType(MAPLE, () -> BYGBlocks.MAPLE_LOG, () -> BYGBlocks.MAPLE_PLANKS, () -> BYGBlocks.MAPLE_SLAB, "byg:block/maple_log", "byg:block/maple_log_top", "byg:block/maple_planks");
        setupWoodType(IMBUED_NIGHTSHADE, () -> BYGBlocks.IMBUED_NIGHTSHADE_LOG, () -> BYGBlocks.NIGHTSHADE_PLANKS, () -> BYGBlocks.NIGHTSHADE_SLAB, "byg:block/imbued_nightshade_log", "byg:block/nightshade_log_top", "byg:block/nightshade_planks");
        setupWoodType(NIGHTSHADE, () -> BYGBlocks.NIGHTSHADE_LOG, () -> BYGBlocks.NIGHTSHADE_PLANKS, () -> BYGBlocks.NIGHTSHADE_SLAB, "byg:block/nightshade_log", "byg:block/nightshade_log_top", "byg:block/nightshade_planks");
        setupWoodType(PALM, () -> BYGBlocks.PALM_LOG, () -> BYGBlocks.PALM_PLANKS, () -> BYGBlocks.PALM_SLAB, "byg:block/palm_log", "byg:block/palm_log_top", "byg:block/palm_planks");
        WoodDataHelper.register(PALO_VERDE, WoodData.of2(() -> BYGBlocks.PALO_VERDE_LOG, () -> Items.BIRCH_PLANKS, () -> Items.BIRCH_SLAB, "byg:block/palo_verde_log", "byg:block/palo_verde_log_top", "minecraft:block/birch_planks"));
        setupWoodType(PINE, () -> BYGBlocks.PINE_LOG, () -> BYGBlocks.PINE_PLANKS, () -> BYGBlocks.PINE_SLAB, "byg:block/pine_log", "byg:block/pine_log_top", "byg:block/pine_planks");
        setupWoodType(RAINBOW_EUCALYPTUS, () -> BYGBlocks.RAINBOW_EUCALYPTUS_LOG, () -> BYGBlocks.RAINBOW_EUCALYPTUS_PLANKS, () -> BYGBlocks.RAINBOW_EUCALYPTUS_SLAB, "byg:block/rainbow_eucalyptus_log", "byg:block/rainbow_eucalyptus_log_top", "byg:block/rainbow_eucalyptus_planks");
        setupWoodType(REDWOOD, () -> BYGBlocks.REDWOOD_LOG, () -> BYGBlocks.REDWOOD_PLANKS, () -> BYGBlocks.REDWOOD_SLAB, "byg:block/redwood_log", "byg:block/redwood_log_top", "byg:block/redwood_planks");
        setupWoodType(SKYRIS, () -> BYGBlocks.SKYRIS_LOG, () -> BYGBlocks.SKYRIS_PLANKS, () -> BYGBlocks.SKYRIS_SLAB, "byg:block/skyris_log", "byg:block/skyris_log_top", "byg:block/skyris_planks");
        setupWoodType(WILLOW, () -> BYGBlocks.WILLOW_LOG, () -> BYGBlocks.WILLOW_PLANKS, () -> BYGBlocks.WILLOW_SLAB, "byg:block/willow_log", "byg:block/willow_log_top", "byg:block/willow_planks");
        setupWoodType(WITCH_HAZEL, () -> BYGBlocks.WITCH_HAZEL_LOG, () -> BYGBlocks.WITCH_HAZEL_PLANKS, () -> BYGBlocks.WITCH_HAZEL_SLAB, "byg:block/witch_hazel_log", "byg:block/witch_hazel_log_top", "byg:block/witch_hazel_planks");
        setupWoodType(ZELKOVA, () -> BYGBlocks.ZELKOVA_LOG, () -> BYGBlocks.ZELKOVA_PLANKS, () -> BYGBlocks.ZELKOVA_SLAB, "byg:block/zelkova_log", "byg:block/zelkova_log_top", "byg:block/zelkova_planks");
        setupWoodType(SYTHIAN, () -> BYGBlocks.SYTHIAN_STEM, () -> BYGBlocks.SYTHIAN_PLANKS, () -> BYGBlocks.SYTHIAN_SLAB, "byg:block/sythian_stem", "byg:block/sythian_stem_top", "byg:block/sythian_planks");
        setupWoodType(EMBUR, () -> BYGBlocks.EMBUR_PEDU, () -> BYGBlocks.EMBUR_PLANKS, () -> BYGBlocks.EMBUR_SLAB, "byg:block/embur_pedu", "byg:block/embur_pedu", "byg:block/embur_planks");
        WoodDataHelper.register(WITHERING_OAK, WoodData.of2(() -> BYGBlocks.WITHERING_OAK_LOG, () -> Items.OAK_PLANKS, () -> Items.OAK_SLAB, "byg:block/withering_oak_log", "byg:block/withering_oak_log_top", "minecraft:block/oak_planks"));
    }

    private static void setupWoodType(WoodType type, Supplier<BlockRegistryObject<?>> log, Supplier<BlockRegistryObject<?>> plank, Supplier<BlockRegistryObject<?>> slab, String logSideTexture, String logTopTexture, String plankTexture)
    {
        WoodDataHelper.register(type, WoodData.of2(() -> log.get().get(), () -> plank.get().get(), () -> slab.get().get(), logSideTexture, logTopTexture, plankTexture));
    }

}