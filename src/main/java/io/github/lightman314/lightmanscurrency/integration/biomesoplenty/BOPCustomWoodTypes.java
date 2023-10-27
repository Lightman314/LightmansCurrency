package io.github.lightman314.lightmanscurrency.integration.biomesoplenty;

import biomesoplenty.api.block.BOPBlocks;
import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import io.github.lightman314.lightmanscurrency.datagen.util.WoodData;
import io.github.lightman314.lightmanscurrency.datagen.util.WoodDataHelper;
import net.minecraft.world.level.material.MaterialColor;

public class BOPCustomWoodTypes {

    private static final String MODID = "biomesoplenty";


    public static final WoodType FIR = WoodType.builder("fir",MODID).ofName("Fir").ofColor(MaterialColor.TERRACOTTA_WHITE).build();
    public static final WoodType REDWOOD = WoodType.builder("redwood", MODID).ofName("Redwood").ofColor(MaterialColor.TERRACOTTA_ORANGE).build();
    public static final WoodType CHERRY = WoodType.builder("cherry", MODID).ofName("Cherry").ofColor(MaterialColor.COLOR_RED).build();
    public static final WoodType MAHOGANY = WoodType.builder("mahogany", MODID).ofName("Mahogany").ofColor(MaterialColor.TERRACOTTA_PINK).build();
    public static final WoodType JACARANDA = WoodType.builder("jacaranda", MODID).ofName("Jacaranda").ofColor(MaterialColor.QUARTZ).build();
    public static final WoodType PALM = WoodType.builder("palm", MODID).ofName("Palm").ofColor(MaterialColor.TERRACOTTA_YELLOW).build();
    public static final WoodType WILLOW = WoodType.builder("willow", MODID).ofName("Willow").ofColor(MaterialColor.TERRACOTTA_LIGHT_GREEN).build();
    public static final WoodType DEAD = WoodType.builder("dead", MODID).ofName("Dead").ofColor(MaterialColor.STONE).build();
    public static final WoodType MAGIC = WoodType.builder("magic", MODID).ofName("Magic").ofColor(MaterialColor.COLOR_BLUE).build();
    public static final WoodType UMBRAN = WoodType.builder("umbran", MODID).ofName("Umbran").ofColor(MaterialColor.TERRACOTTA_BLUE).build();
    public static final WoodType HELLBARK = WoodType.builder("hellbark", MODID).ofName("Hellbark").ofColor(MaterialColor.TERRACOTTA_GRAY).build();

    public static void setupWoodTypes()
    {
        WoodDataHelper.register(FIR, WoodData.of1(() -> BOPBlocks.FIR_LOG, () -> BOPBlocks.FIR_PLANKS, () -> BOPBlocks.FIR_SLAB, "biomesoplenty:block/fir_log","biomesoplenty:block/fir_log_top","biomesoplenty:block/fir_planks"));
        WoodDataHelper.register(REDWOOD, WoodData.of1(() -> BOPBlocks.REDWOOD_LOG, () -> BOPBlocks.REDWOOD_PLANKS, () -> BOPBlocks.REDWOOD_SLAB, "biomesoplenty:block/redwood_log", "biomesoplenty:block/redwood_log_top","biomesoplenty:block/redwood_planks"));
        WoodDataHelper.register(CHERRY, WoodData.of1(() -> BOPBlocks.CHERRY_LOG, () -> BOPBlocks.CHERRY_PLANKS, () -> BOPBlocks.CHERRY_SLAB, "biomesoplenty:block/cherry_log", "biomesoplenty:block/cherry_log_top", "biomesoplenty:block/cherry_planks"));
        WoodDataHelper.register(MAHOGANY, WoodData.of1(() -> BOPBlocks.MAHOGANY_LOG, () -> BOPBlocks.MAHOGANY_PLANKS, () -> BOPBlocks.MAHOGANY_SLAB, "biomesoplenty:block/mahogany_log","biomesoplenty:block/mahogany_log_top","biomesoplenty:block/mahogany_planks"));
        WoodDataHelper.register(JACARANDA, WoodData.of1(() -> BOPBlocks.JACARANDA_LOG, () -> BOPBlocks.JACARANDA_PLANKS, () -> BOPBlocks.JACARANDA_SLAB, "biomesoplenty:block/jacaranda_log","biomesoplenty:block/jacaranda_log_top","biomesoplenty:block/jacaranda_planks"));
        WoodDataHelper.register(PALM, WoodData.of1(() -> BOPBlocks.PALM_LOG, () -> BOPBlocks.PALM_PLANKS, () -> BOPBlocks.PALM_SLAB, "biomesoplenty:block/palm_log","biomesoplenty:block/palm_log_top","biomesoplenty:block/palm_planks"));
        WoodDataHelper.register(WILLOW, WoodData.of1(() -> BOPBlocks.WILLOW_LOG, () -> BOPBlocks.WILLOW_PLANKS, () -> BOPBlocks.WILLOW_SLAB, "biomesoplenty:block/willow_log","biomesoplenty:block/willow_log_top","biomesoplenty:block/willow_planks"));
        WoodDataHelper.register(DEAD, WoodData.of1(() -> BOPBlocks.DEAD_LOG, () -> BOPBlocks.DEAD_PLANKS, () -> BOPBlocks.DEAD_SLAB, "biomesoplenty:block/dead_log","biomesoplenty:block/dead_log_top","biomesoplenty:block/dead_planks"));
        WoodDataHelper.register(MAGIC, WoodData.of1(() -> BOPBlocks.MAGIC_LOG, () -> BOPBlocks.MAGIC_PLANKS, () -> BOPBlocks.MAGIC_SLAB, "biomesoplenty:block/magic_log","biomesoplenty:block/magic_log_top","biomesoplenty:block/magic_planks"));
        WoodDataHelper.register(UMBRAN, WoodData.of1(() -> BOPBlocks.UMBRAN_LOG, () -> BOPBlocks.UMBRAN_PLANKS, () -> BOPBlocks.UMBRAN_SLAB, "biomesoplenty:block/umbran_log", "biomesoplenty:block/umbran_log_top", "biomesoplenty:block/umbran_planks"));
        WoodDataHelper.register(HELLBARK, WoodData.of1(() -> BOPBlocks.HELLBARK_LOG, () -> BOPBlocks.HELLBARK_PLANKS, () -> BOPBlocks.HELLBARK_SLAB, "biomesoplenty:block/hellbark_log","biomesoplenty:block/hellbark_log_top","biomesoplenty:block/hellbark_planks"));
    }

}