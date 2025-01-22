package io.github.lightman314.lightmanscurrency.datagen.common.structures;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.common.loot.LCLootTables;
import io.github.lightman314.lightmanscurrency.common.world.LCStructures;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.AppendLoot;
import net.minecraft.world.level.storage.loot.LootTable;

import javax.annotation.Nonnull;

public class LCProcessorListProvider {

    public static void bootstrap(@Nonnull BootstrapContext<StructureProcessorList> context)
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
        /*context.register(LCVillages.PROCESSOR_IDAS_TAIGA_LARGE_BANK, new StructureProcessorList(Lists.newArrayList(
                gravelArchaeology(LCLootTables.ARCHAEOLOGY_VILLAGE_IDAS_TAIGA_LARGE_BANK,6),
                //Normal Chest Loot
                chestLoot(LCLootTables.CHEST_VILLAGE_IDAS_TAIGA_LARGE_BANK, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.NORTH))
                //Vault Chest Loot
                //EAST, SOUTH, WEST
                chestLoot(LCLootTables.CHEST_VILLAGE_IDAS_TAIGA_LARGE_BANK_VAULT, //TODO )
        )));*/

        //Ancient Ruins
        context.register(LCStructures.PROCESSOR_ANCIENT_RUINS, new StructureProcessorList(Lists.newArrayList(
                gravelArchaeology(LCLootTables.ARCHAEOLOGY_ANCIENT_RUINS,6)
        )));

    }

    private static CappedProcessor gravelArchaeology(ResourceLocation lootTable, int count) { return gravelArchaeology(ResourceKey.create(Registries.LOOT_TABLE,lootTable),count); }
    private static CappedProcessor gravelArchaeology(ResourceKey<LootTable> lootTable, int count) { return archaeologyRule(lootTable,Blocks.GRAVEL,Blocks.SUSPICIOUS_GRAVEL.defaultBlockState(),count); }
    private static CappedProcessor sandArchaeology(ResourceLocation lootTable, int count) { return sandArchaeology(ResourceKey.create(Registries.LOOT_TABLE,lootTable),count); }
    private static CappedProcessor sandArchaeology(ResourceKey<LootTable> lootTable, int count) { return archaeologyRule(lootTable,Blocks.SAND,Blocks.SUSPICIOUS_SAND.defaultBlockState(),count); }
    private static CappedProcessor archaeologyRule(ResourceLocation lootTable, Block original, BlockState replacement, int count) { return archaeologyRule(ResourceKey.create(Registries.LOOT_TABLE,lootTable), original,replacement,count); }
    private static CappedProcessor archaeologyRule(ResourceKey<LootTable> lootTable, Block original, BlockState replacement, int count)
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

    private static RuleProcessor chestLoot(ResourceLocation lootTable, BlockState state) { return chestLoot(ResourceKey.create(Registries.LOOT_TABLE,lootTable),state); }
    private static RuleProcessor chestLoot(ResourceKey<LootTable> lootTable, BlockState state) {
        return new RuleProcessor(
                Lists.newArrayList(
                        new ProcessorRule(
                                new BlockMatchTest(state.getBlock()),
                                AlwaysTrueTest.INSTANCE,
                                PosAlwaysTrueTest.INSTANCE,
                                state,
                                new AppendLoot(lootTable)
                        )
                )
        );
    }

}
