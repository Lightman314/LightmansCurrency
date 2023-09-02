package io.github.lightman314.lightmanscurrency.integration.byg;

import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType.Attributes;
import io.github.lightman314.lightmanscurrency.datagen.util.WoodData;
import io.github.lightman314.lightmanscurrency.datagen.util.WoodDataHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.MaterialColor;
import potionstudios.byg.common.block.BYGBlocks;
import potionstudios.byg.common.block.BYGWoodTypes;

import java.util.function.Supplier;

public class BYGCustomWoodTypes {

    private static final String MODID = "byg";

    public static final WoodType ASPEN = WoodType.builder("aspen", MODID).ofName("Aspen").ofColor(MaterialColor.TERRACOTTA_YELLOW).build();
    public static final WoodType BAOBAB = WoodType.builder("baobab", MODID).ofName("Baobab").ofColor(MaterialColor.TERRACOTTA_GREEN).build();
    public static final WoodType IMBUED_BLUE_ENCHANTED = WoodType.builder("imbued_blue_enchanted", MODID).ofName("Imbued Blue Enchanted").ofColor(MaterialColor.COLOR_BLUE).withAttributes(Attributes.LOG_ONLY).build();
    public static final WoodType BLUE_ENCHANTED = WoodType.builder("blue_enchanted", MODID).ofName("Blue Enchanted").ofColor(MaterialColor.COLOR_BLUE).build();
    public static final WoodType BULBIS = WoodType.builder("bulbis", MODID).ofName("Bulbis").ofColor(MaterialColor.COLOR_BLUE).build();
    public static final WoodType CHERRY = WoodType.builder("cherry", MODID).ofName("Cherry").ofColor(MaterialColor.WOOD).build();
    public static final WoodType CIKA = WoodType.builder("cika", MODID).ofName("Cika").ofColor(MaterialColor.TERRACOTTA_ORANGE).build();
    public static final WoodType CYPRESS = WoodType.builder("cypress", MODID).ofName("Cypress").ofColor(MaterialColor.TERRACOTTA_LIGHT_GREEN).build();
    public static final WoodType EBONY = WoodType.builder("ebony", MODID).ofName("Ebony").ofColor(MaterialColor.COLOR_GREEN).build();
    public static final WoodType ETHER = WoodType.builder("ether", MODID).ofName("Ether").ofColor(MaterialColor.COLOR_CYAN).build();
    public static final WoodType FIR = WoodType.builder("fir", MODID).ofName("Fir").ofColor(MaterialColor.COLOR_GREEN).build();
    public static final WoodType IMBUED_GREEN_ENCHANTED = WoodType.builder("imbued_green_enchanted", MODID).ofName("Imbued Green Enchanted").ofColor(MaterialColor.COLOR_GREEN).withAttributes(Attributes.LOG_ONLY).build();
    public static final WoodType GREEN_ENCHANTED = WoodType.builder("green_enchanted", MODID).ofName("Green Enchanted").ofColor(MaterialColor.COLOR_GREEN).build();
    public static final WoodType HOLLY = WoodType.builder("holly", MODID).ofName("Holly").ofColor(MaterialColor.TERRACOTTA_GREEN).build();
    public static final WoodType FUNGAL_IMPARIUS = WoodType.builder("fungal_imparius", MODID).ofName("Fungal Imparius").ofColor(MaterialColor.COLOR_BLUE).withAttributes(Attributes.LOG_ONLY).build();
    public static final WoodType IMPARIUS = WoodType.builder("imparius", MODID).ofName("Imparius").ofColor(MaterialColor.COLOR_BLUE).build();
    public static final WoodType LAMENT = WoodType.builder("lament", MODID).ofName("Lament").ofColor(MaterialColor.COLOR_MAGENTA).build();
    public static final WoodType JACARANDA = WoodType.builder("jacaranda", MODID).ofName("Jacaranda").ofColor(MaterialColor.TERRACOTTA_PURPLE).build();
    public static final WoodType MAHOGANY = WoodType.builder("mahogany", MODID).ofName("Mahogany").ofColor(MaterialColor.COLOR_GREEN).build();
    public static final WoodType WHITE_MANGROVE = WoodType.builder("white_mangrove", MODID).ofName("White Mangrove").ofColor(MaterialColor.COLOR_GREEN).build();
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
        setupWoodType(ASPEN, BYGWoodTypes.ASPEN, "byg:block/aspen/log", "byg:block/aspen/log_top", "byg:block/aspen/planks");
        setupWoodType(BAOBAB, BYGWoodTypes.BAOBAB, "byg:block/baobab/log", "byg:block/baobab/log_top", "byg:block/baobab/planks");
        setupWoodTypeWithLog(IMBUED_BLUE_ENCHANTED, BYGWoodTypes.BLUE_ENCHANTED, () -> BYGBlocks.IMBUED_BLUE_ENCHANTED_LOG, "byg:block/blue_enchanted/log2", "byg:block/blue_enchanted/log_top", "byg:block/blue_enchanted/planks");
        setupWoodType(BLUE_ENCHANTED, BYGWoodTypes.BLUE_ENCHANTED, "byg:block/blue_enchanted/log", "byg:block/blue_enchanted/log_top", "byg:block/blue_enchanted/planks");
        setupWoodType(BULBIS, BYGWoodTypes.BULBIS, "byg:block/bulbis/log", "byg:block/bulbis/log_top", "byg:block/bulbis/planks");
        setupWoodType(CHERRY, BYGWoodTypes.CHERRY, "byg:block/cherry/log", "byg:block/cherry/log_top", "byg:block/cherry/planks");
        setupWoodType(CIKA, BYGWoodTypes.CIKA, "byg:block/cika/log", "byg:block/cika/log_top", "byg:block/cika/planks");
        setupWoodType(CYPRESS, BYGWoodTypes.CYPRESS, "byg:block/cypress/log", "byg:block/cypress/log_top", "byg:block/cypress/planks");
        setupWoodType(EBONY, BYGWoodTypes.EBONY, "byg:block/ebony/log", "byg:block/ebony/log_top", "byg:block/ebony/planks");
        setupWoodType(ETHER, BYGWoodTypes.ETHER, "byg:block/ether/log", "byg:block/ether/log_top", "byg:block/ether/planks");
        setupWoodType(FIR, BYGWoodTypes.FIR, "byg:block/fir/log", "byg:block/fir/log_top", "byg:block/fir/planks");
        setupWoodTypeWithLog(IMBUED_GREEN_ENCHANTED, BYGWoodTypes.GREEN_ENCHANTED, () -> BYGBlocks.IMBUED_GREEN_ENCHANTED_LOG, "byg:block/green_enchanted/log2", "byg:block/green_enchanted/log_top", "byg:block/green_enchanted/planks");
        setupWoodType(GREEN_ENCHANTED, BYGWoodTypes.GREEN_ENCHANTED, "byg:block/green_enchanted/log", "byg:block/green_enchanted/log_top", "byg:block/green_enchanted/planks");
        setupWoodType(HOLLY, BYGWoodTypes.HOLLY, "byg:block/holly/log", "byg:block/holly/log_top", "byg:block/holly/planks");
        setupWoodTypeWithLog(FUNGAL_IMPARIUS, BYGWoodTypes.IMPARIUS, () -> BYGBlocks.FUNGAL_IMPARIUS_STEM, "byg:block/fungal_imparius_stem", "byg:block/fungal_imparius_stem_top", "byg:block/imparius/planks");
        setupWoodType(IMPARIUS, BYGWoodTypes.IMPARIUS, "byg:block/imparius/log", "byg:block/imparius/log_top", "byg:block/imparius/planks");
        setupWoodType(LAMENT, BYGWoodTypes.LAMENT, "byg:block/lament/log", "byg:block/lament/log_top", "byg:block/lament/planks");
        setupWoodType(JACARANDA, BYGWoodTypes.JACARANDA, "byg:block/jacaranda/log", "byg:block/jacaranda/log_top", "byg:block/jacaranda/planks");
        setupWoodType(MAHOGANY, BYGWoodTypes.MAHOGANY, "byg:block/mahogany/log", "byg:block/mahogany/log_top", "byg:block/mahogany/planks");
        setupWoodType(WHITE_MANGROVE, BYGWoodTypes.WHITE_MANGROVE, "byg:block/white_mangrove/log", "byg:block/white_mangrove/log_top", "byg:block/white_mangrove/planks");
        setupWoodType(MAPLE, BYGWoodTypes.MAPLE, "byg:block/maple/log", "byg:block/maple/log_top", "byg:block/maple/planks");
        setupWoodTypeWithLog(IMBUED_NIGHTSHADE, BYGWoodTypes.NIGHTSHADE, () -> BYGBlocks.IMBUED_NIGHTSHADE_LOG, "byg:block/imbued_nightshade_log", "byg:block/nightshade/log_top", "byg:block/nightshade/planks");
        setupWoodType(NIGHTSHADE, BYGWoodTypes.NIGHTSHADE, "byg:block/nightshade/log", "byg:block/nightshade/log_top", "byg:block/nightshade/planks");
        setupWoodType(PALM, BYGWoodTypes.PALM, "byg:block/palm/log", "byg:block/palm/log_top", "byg:block/palm/planks");
        WoodDataHelper.register(PALO_VERDE, WoodData.of2(() -> BYGBlocks.PALO_VERDE_LOG, () -> Items.BIRCH_PLANKS, () -> Items.BIRCH_SLAB, "byg:block/palo_verde_log", "byg:block/palo_verde_log_top", "minecraft:block/birch_planks"));
        setupWoodType(PINE, BYGWoodTypes.PINE, "byg:block/pine/log", "byg:block/pine/log_top", "byg:block/pine/planks");
        setupWoodType(RAINBOW_EUCALYPTUS, BYGWoodTypes.RAINBOW_EUCALYPTUS, "byg:block/rainbow_eucalyptus/log", "byg:block/rainbow_eucalyptus/log_top", "byg:block/rainbow_eucalyptus/planks");
        setupWoodType(REDWOOD, BYGWoodTypes.REDWOOD, "byg:block/redwood/log", "byg:block/redwood/log_top", "byg:block/redwood/planks");
        setupWoodType(SKYRIS, BYGWoodTypes.SKYRIS, "byg:block/skyris/log", "byg:block/skyris/log_top", "byg:block/skyris/planks");
        setupWoodType(WILLOW, BYGWoodTypes.WILLOW, "byg:block/willow/log", "byg:block/willow/log_top", "byg:block/willow/planks");
        setupWoodType(WITCH_HAZEL, BYGWoodTypes.WITCH_HAZEL, "byg:block/witch_hazel/log", "byg:block/witch_hazel/log_top", "byg:block/witch_hazel/planks");
        setupWoodType(ZELKOVA, BYGWoodTypes.ZELKOVA, "byg:block/zelkova/log", "byg:block/zelkova/log_top", "byg:block/zelkova/planks");
        setupWoodType(SYTHIAN, BYGWoodTypes.SYTHIAN, "byg:block/sythian/log", "byg:block/sythian/log_top", "byg:block/sythian/planks");
        setupWoodType(EMBUR, BYGWoodTypes.EMBUR, "byg:block/embur/log", "byg:block/embur/log_top", "byg:block/embur/planks");
        WoodDataHelper.register(WITHERING_OAK, WoodData.of2(() -> BYGBlocks.WITHERING_OAK_LOG, () -> Items.OAK_PLANKS, () -> Items.OAK_SLAB, "byg:block/withering_oak_log", "byg:block/withering_oak_log_top", "minecraft:block/oak_planks"));
    }

    private static void setupWoodType(WoodType type, BYGWoodTypes bygType, String logSideTexture, String logTopTexture, String plankTexture)
    {
        WoodDataHelper.register(type, WoodData.of2(bygType::log, bygType::planks, bygType::slab, logSideTexture, logTopTexture, plankTexture));
    }

    private static void setupWoodTypeWithLog(WoodType type, BYGWoodTypes bygType, Supplier<ItemLike> customLog, String logSideTexture, String logTopTexture, String plankTexture)
    {
        WoodDataHelper.register(type, WoodData.of2(customLog, bygType::planks, bygType::slab, logSideTexture, logTopTexture, plankTexture));
    }

}
