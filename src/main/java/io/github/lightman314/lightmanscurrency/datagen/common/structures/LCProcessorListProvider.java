package io.github.lightman314.lightmanscurrency.datagen.common.structures;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.common.loot.LCLootTables;
import io.github.lightman314.lightmanscurrency.common.world.LCStructures;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.AppendLoot;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

public class LCProcessorListProvider {

    public static void bootstrap(@Nonnull BootstapContext<StructureProcessorList> context)
    {

        //Village House Processors
        context.register(LCStructures.PROCESSOR_DESERT_BANKER_ARCHAEOLOGY, new StructureProcessorList(Lists.newArrayList(
                archaeologyRule(LCLootTables.ARCHAEOLOGY_VILLAGE_DESERT_BANKER,Blocks.SUSPICIOUS_SAND,Blocks.SUSPICIOUS_SAND.defaultBlockState(),10)
        )));

        context.register(LCStructures.PROCESSOR_PLAINS_SHOP_ARCHAEOLOGY, new StructureProcessorList(Lists.newArrayList(
                gravelArchaeology(LCLootTables.ARCHAEOLOGY_VILLAGE_PLAINS_SHOP,4)
        )));

        context.register(LCStructures.PROCESSOR_TAIGA_SHOP_ARCHAEOLOGY, new StructureProcessorList(Lists.newArrayList(
                gravelArchaeology(LCLootTables.ARCHAEOLOGY_VILLAGE_TAIGA_SHOP,4))
        ));

        context.register(LCStructures.PROCESSOR_DESERT_SHOP_ARCHAEOLOGY, new StructureProcessorList(Lists.newArrayList(
                sandArchaeology(LCLootTables.ARCHAEOLOGY_VILLAGE_DESERT_SHOP,10))
        ));

        //IDAS
        context.register(LCStructures.PROCESSOR_IDAS_TAIGA_LARGE_BANK, new StructureProcessorList(Lists.newArrayList(
                gravelArchaeology(LCLootTables.ARCHAEOLOGY_VILLAGE_IDAS_TAIGA_LARGE_BANK,6)
        )));

        //Ancient Ruins
        context.register(LCStructures.PROCESSOR_ANCIENT_RUINS, new StructureProcessorList(Lists.newArrayList(
                gravelArchaeology(LCLootTables.ARCHAEOLOGY_ANCIENT_RUINS,6)
        )));

    }

    private static CappedProcessor gravelArchaeology(ResourceLocation lootTable, int count) { return archaeologyRule(lootTable,Blocks.GRAVEL,Blocks.SUSPICIOUS_GRAVEL.defaultBlockState(),count); }
    private static CappedProcessor sandArchaeology(ResourceLocation lootTable, int count) { return archaeologyRule(lootTable,Blocks.SAND,Blocks.SUSPICIOUS_SAND.defaultBlockState(),count); }
    private static CappedProcessor archaeologyRule(ResourceLocation lootTable, Block original, BlockState replacement, int count)
    {
        return new CappedProcessor(
                new RuleProcessor(
                        Lists.newArrayList(
                                new ProcessorRule(
                                        new BlockMatchTest(original),
                                        AlwaysTrueTest.INSTANCE,
                                        PosAlwaysTrueTest.INSTANCE,
                                        replacement,
                                        new AppendLoot(lootTable)
                                )
                        )
                ),
                ConstantInt.of(count)
        );
    }

}