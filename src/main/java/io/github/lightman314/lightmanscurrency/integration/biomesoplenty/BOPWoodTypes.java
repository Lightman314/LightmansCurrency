package io.github.lightman314.lightmanscurrency.integration.biomesoplenty;

import biomesoplenty.api.block.BOPBlocks;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.variants.ModdedWoodType;
import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import io.github.lightman314.lightmanscurrency.datagen.util.WoodData;
import io.github.lightman314.lightmanscurrency.datagen.util.WoodDataHelper;
import net.minecraft.world.level.material.MaterialColor;

public class BOPWoodTypes {

    private static final String MODID = "biomesoplenty";


    public static final WoodType FIR = new ModdedWoodType("fir", MaterialColor.TERRACOTTA_WHITE, MODID);
    public static final WoodType REDWOOD = new ModdedWoodType("redwood", MaterialColor.TERRACOTTA_ORANGE, MODID);
    public static final WoodType CHERRY = new ModdedWoodType("cherry", MaterialColor.COLOR_RED, MODID);
    public static final WoodType MAHOGANY = new ModdedWoodType("mahogany", MaterialColor.TERRACOTTA_PINK, MODID);
    public static final WoodType JACARANDA = new ModdedWoodType("jacaranda", MaterialColor.QUARTZ, MODID);
    public static final WoodType PALM = new ModdedWoodType("palm", MaterialColor.TERRACOTTA_YELLOW, MODID);
    public static final WoodType WILLOW = new ModdedWoodType("willow", MaterialColor.TERRACOTTA_LIGHT_GREEN, MODID);
    public static final WoodType DEAD = new ModdedWoodType("dead", MaterialColor.STONE, MODID);
    public static final WoodType MAGIC = new ModdedWoodType("magic", MaterialColor.COLOR_BLUE, MODID);
    public static final WoodType UMBRAN = new ModdedWoodType("umbran", MaterialColor.TERRACOTTA_BLUE, MODID);
    public static final WoodType HELLBARK = new ModdedWoodType("hellbark", MaterialColor.TERRACOTTA_GRAY, MODID);

    public static void setupWoodTypes()
    {
        try{
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
        } catch(Throwable t) { LightmansCurrency.LogWarning("Error setting up BOP wood types! BOP has probably changed their API!", t); }
    }

}