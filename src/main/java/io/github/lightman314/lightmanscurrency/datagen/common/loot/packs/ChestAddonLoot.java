package io.github.lightman314.lightmanscurrency.datagen.common.loot.packs;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.common.loot.ConfigItemTier;
import io.github.lightman314.lightmanscurrency.common.loot.LCLootTables;
import io.github.lightman314.lightmanscurrency.common.loot.entries.ConfigLoot;
import io.github.lightman314.lightmanscurrency.datagen.common.loot.SimpleSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.List;

public class ChestAddonLoot extends SimpleSubProvider {

    public ChestAddonLoot(HolderLookup.Provider lookup) { super(lookup); }

    @Override
    public void generate() {

        ChestLootEntryData T1 = new ChestLootEntryData(ConfigItemTier.T1, 1, 10, 1);
        ChestLootEntryData T2 = new ChestLootEntryData(ConfigItemTier.T2, 1, 10, 2);
        ChestLootEntryData T3 = new ChestLootEntryData(ConfigItemTier.T3, 1, 10, 3);
        ChestLootEntryData T4 = new ChestLootEntryData(ConfigItemTier.T4, 1, 10, 4);
        ChestLootEntryData T5 = new ChestLootEntryData(ConfigItemTier.T5, 1, 8, 5);
        ChestLootEntryData T6 = new ChestLootEntryData(ConfigItemTier.T6, 1, 3, 6);

        this.register(LCLootTables.CHEST_DROPS_T1, LootTable.lootTable().withPool(GenerateChestCoinPool(ImmutableList.of(T1), 1, 5)));
        this.register(LCLootTables.CHEST_DROPS_T2, LootTable.lootTable().withPool(GenerateChestCoinPool(ImmutableList.of(T1,T2), 1, 5)));
        this.register(LCLootTables.CHEST_DROPS_T3, LootTable.lootTable().withPool(GenerateChestCoinPool(ImmutableList.of(T1,T2,T3), 2, 6)));
        this.register(LCLootTables.CHEST_DROPS_T4, LootTable.lootTable().withPool(GenerateChestCoinPool(ImmutableList.of(T1,T2,T3,T4), 3, 6)));
        this.register(LCLootTables.CHEST_DROPS_T5, LootTable.lootTable().withPool(GenerateChestCoinPool(ImmutableList.of(T1,T2,T3,T4,T5), 3, 6)));
        this.register(LCLootTables.CHEST_DROPS_T6, LootTable.lootTable().withPool(GenerateChestCoinPool(ImmutableList.of(T1,T2,T3,T4,T5,T6), 3, 6)));

    }

    private static LootPool.Builder GenerateChestCoinPool(List<ChestLootEntryData> lootEntries, float minRolls, float maxRolls)
    {
        LootPool.Builder builder = LootPool.lootPool()
                .setRolls(UniformGenerator.between(minRolls, maxRolls));
        //Add each loot entry
        for(ChestLootEntryData entry : lootEntries)
            builder.add(ConfigLoot.lootTableTier(entry.tier).apply(SetItemCountFunction.setCount(UniformGenerator.between(entry.minCount, entry.maxCount))).setWeight(entry.weight));
        return builder;
    }

    private record ChestLootEntryData(ConfigItemTier tier, float minCount, float maxCount, int weight) { }

}
