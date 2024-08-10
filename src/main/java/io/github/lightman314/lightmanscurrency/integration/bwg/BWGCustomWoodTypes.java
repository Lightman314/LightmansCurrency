package io.github.lightman314.lightmanscurrency.integration.bwg;

import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import io.github.lightman314.lightmanscurrency.datagen.util.WoodData;
import io.github.lightman314.lightmanscurrency.datagen.util.WoodDataHelper;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.MapColor;
import net.potionstudios.biomeswevegone.world.level.block.wood.BWGWood;
import net.potionstudios.biomeswevegone.world.level.block.wood.BWGWoodSet;

import java.util.function.Supplier;

public class BWGCustomWoodTypes {

    private static final String MODID = "biomeswevegone";

    public static final WoodType ASPEN = WoodType.builder("aspen",MODID).ofName("Aspen").ofColor(MapColor.QUARTZ).build();
    public static final WoodType BAOBAB = WoodType.builder("baobab",MODID).ofName("Baobab").ofColor(MapColor.TERRACOTTA_WHITE).build();
    //BULBIS
    public static final WoodType BLUE_ENCHANTED = WoodType.builder("blue_enchanted",MODID).ofName("Blue Enchanted").ofColor(MapColor.COLOR_BLUE).build();
    public static final WoodType CIKA = WoodType.builder("cika",MODID).ofName("Cika").ofColor(MapColor.TERRACOTTA_WHITE).build();
    public static final WoodType CYPRESS = WoodType.builder("cypress",MODID).ofName("Cypress").ofColor(MapColor.TERRACOTTA_WHITE).build();
    public static final WoodType EBONY = WoodType.builder("ebony",MODID).ofName("Ebony").ofColor(MapColor.COLOR_BLACK).build();
    //EMBUR
    //ETHER
    public static final WoodType FIR = WoodType.builder("fir",MODID).ofName("Fir").ofColor(MapColor.TERRACOTTA_WHITE).build();
    public static final WoodType FLORUS = WoodType.builder("florus",MODID).ofName("Florus").ofColor(MapColor.COLOR_GREEN).build();
    public static final WoodType GREEN_ENCHANTED = WoodType.builder("green_enchanted",MODID).ofName("Green Enchanted").ofColor(MapColor.COLOR_LIGHT_GREEN).build();
    public static final WoodType HOLLY = WoodType.builder("holly",MODID).ofName("Holly").ofColor(MapColor.TERRACOTTA_WHITE).build();
    public static final WoodType IRONWOOD = WoodType.builder("ironwood",MODID).ofName("Ironwood").ofColor(MapColor.COLOR_GRAY).build();
    public static final WoodType JACARANDA = WoodType.builder("jacaranda",MODID).ofName("Jacaranda").ofColor(MapColor.COLOR_PINK).build();
    public static final WoodType MAHOGANY = WoodType.builder("mahogany",MODID).ofName("Mahogany").ofColor(MapColor.COLOR_PINK).build();
    public static final WoodType MAPLE = WoodType.builder("maple",MODID).ofName("Maple").ofColor(MapColor.TERRACOTTA_WHITE).build();
    public static final WoodType PALM = WoodType.builder("palm",MODID).ofName("Palm").ofColor(MapColor.TERRACOTTA_WHITE).build();
    public static final WoodType PINE = WoodType.builder("pine",MODID).ofName("Pine").ofColor(MapColor.TERRACOTTA_WHITE).build();
    public static final WoodType RAINBOW_EUCALYPTUS = WoodType.builder("rainbow_eucalyptus",MODID).ofName("Rainbow Eucalyptus").ofColor(MapColor.TERRACOTTA_WHITE).build();
    public static final WoodType REDWOOD = WoodType.builder("redwood",MODID).ofName("Redwood").ofColor(MapColor.COLOR_RED).build();
    public static final WoodType SAKURA = WoodType.builder("sakura",MODID).ofName("Redwood").ofColor(MapColor.COLOR_RED).build();
    public static final WoodType SKYRIS = WoodType.builder("skyris",MODID).ofName("Skyris").ofColor(MapColor.COLOR_LIGHT_BLUE).build();
    public static final WoodType WHITE_MANGROVE = WoodType.builder("white_mangrove",MODID).ofName("White Mangrove").ofColor(MapColor.TERRACOTTA_WHITE).build();
    public static final WoodType WILLOW = WoodType.builder("willow",MODID).ofName("Willow").ofColor(MapColor.COLOR_GREEN).build();
    public static final WoodType WITCH_HAZEL = WoodType.builder("witch_hazel",MODID).ofName("Witch Hazel").ofColor(MapColor.COLOR_GREEN).build();
    public static final WoodType ZELKOVA = WoodType.builder("zelkova",MODID).ofName("Zelkova").ofColor(MapColor.COLOR_ORANGE).build();

    //Log-Only Wood Types
    public static final WoodType IMBUED_BLUE_ENCHANTED = WoodType.builder("imbued_blue_enchanted",MODID).ofName("Imbued Blue Enchanted").ofColor(MapColor.COLOR_BLUE).withAttributes(WoodType.Attributes.LOG_ONLY).build();
    public static final WoodType IMBUED_GREEN_ENCHANTED = WoodType.builder("imbued_green_enchanted",MODID).ofName("Imbued Green Enchanted").ofColor(MapColor.COLOR_LIGHT_GREEN).withAttributes(WoodType.Attributes.LOG_ONLY).build();
    public static final WoodType PALO_VERDE = WoodType.builder("palo_verde",MODID).ofName("Palo Verde").ofColor(MapColor.COLOR_GREEN).withAttributes(WoodType.Attributes.LOG_ONLY).build();

    public static void setupWoodTypes()
    {
        //Normal Wood Types
        setupWoodType(ASPEN, BWGWood.ASPEN);
        setupWoodType(BAOBAB, BWGWood.BAOBAB);
        //BULBIS
        setupWoodType(BLUE_ENCHANTED, BWGWood.BLUE_ENCHANTED);
        setupWoodType(CIKA, BWGWood.CIKA);
        setupWoodType(CYPRESS, BWGWood.CYPRESS);
        setupWoodType(EBONY, BWGWood.EBONY);
        //EMBUR
        //ETHER
        setupWoodType(FIR, BWGWood.FIR);
        setupWoodTypeStemmed(FLORUS, BWGWood.FLORUS);
        setupWoodType(GREEN_ENCHANTED, BWGWood.GREEN_ENCHANTED);
        setupWoodType(HOLLY, BWGWood.HOLLY);
        //IMPARIUS
        setupWoodType(IRONWOOD, BWGWood.IRONWOOD);
        setupWoodType(JACARANDA, BWGWood.JACARANDA);
        setupWoodType(MAHOGANY, BWGWood.MAHOGANY);
        setupWoodType(MAPLE, BWGWood.MAPLE);
        //NIGHTSHADE
        setupWoodType(PALM, BWGWood.PALM);
        setupWoodType(PINE, BWGWood.PINE);
        setupWoodType(RAINBOW_EUCALYPTUS, BWGWood.RAINBOW_EUCALYPTUS);
        setupWoodType(REDWOOD, BWGWood.REDWOOD);
        setupWoodType(SAKURA, BWGWood.SAKURA);
        setupWoodType(SKYRIS, BWGWood.SKYRIS);
        //SYTHIAN
        setupWoodType(WHITE_MANGROVE, BWGWood.WHITE_MANGROVE);
        setupWoodType(WILLOW, BWGWood.WILLOW);
        setupWoodType(WITCH_HAZEL, BWGWood.WITCH_HAZEL);
        setupWoodType(ZELKOVA, BWGWood.ZELKOVA);

        //Log-Only Wood Types
        setupWoodTypeWithLog(IMBUED_BLUE_ENCHANTED, BWGWood.BLUE_ENCHANTED, BWGWood.IMBUED_BLUE_ENCHANTED_WOOD, getTexture(BLUE_ENCHANTED,"imbued_wood"),getTexture(BLUE_ENCHANTED,"log_top"),getTexture(BLUE_ENCHANTED,"planks"));
        setupWoodTypeWithLog(IMBUED_GREEN_ENCHANTED, BWGWood.GREEN_ENCHANTED, BWGWood.IMBUED_GREEN_ENCHANTED_WOOD, getTexture(GREEN_ENCHANTED,"imbued_wood"),getTexture(GREEN_ENCHANTED,"log_top"),getTexture(GREEN_ENCHANTED,"planks"));
        WoodDataHelper.register(PALO_VERDE, WoodData.of2(BWGWood.PALO_VERDE_LOG,null,null,getTexture(PALO_VERDE,"log"),getTexture(PALO_VERDE,"log_top"),getTexture(PALO_VERDE,"log")));

    }

    private static void setupWoodType(WoodType type, BWGWoodSet bygType) { WoodDataHelper.register(type, WoodData.of2(bygType::logstem,bygType::planks,bygType::slab,getTexture(type,"log"),getTexture(type,"log_top"),getTexture(type,"planks"))); }
    private static void setupWoodTypeStemmed(WoodType type, BWGWoodSet bygType) { WoodDataHelper.register(type, WoodData.of2(bygType::logstem,bygType::planks,bygType::slab,getTexture(type,"stem"),getTexture(type,"stem_top"),getTexture(type,"planks"))); }

    private static void setupWoodTypeWithLog(WoodType type, BWGWoodSet bygType, Supplier<? extends ItemLike> customLog, String logSideTexture, String logTopTexture, String plankTexture)
    {
        WoodDataHelper.register(type, WoodData.of2(customLog,bygType::planks,bygType::slab,logSideTexture,logTopTexture,plankTexture));
    }

    private static String getTexture(WoodType type, String texture) { return getTexture(type.id,texture); }
    private static String getTexture(String folder, String texture) { return MODID + ":block/" + folder + "/" + texture; }

}
