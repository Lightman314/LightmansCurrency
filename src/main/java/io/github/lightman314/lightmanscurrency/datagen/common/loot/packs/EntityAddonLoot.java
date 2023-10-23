package io.github.lightman314.lightmanscurrency.datagen.common.loot.packs;

import io.github.lightman314.lightmanscurrency.common.loot.ConfigItemTier;
import io.github.lightman314.lightmanscurrency.common.loot.LCLootTables;
import io.github.lightman314.lightmanscurrency.common.loot.entries.ConfigLoot;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;

public class EntityAddonLoot implements LootTableSubProvider {

    @Override
    public void generate(@Nonnull BiConsumer<ResourceLocation, LootTable.Builder> consumer) {

        //Generate default entity pools
        LootPool.Builder t1 = GenerateEntityCoinPool(ConfigItemTier.T1, 1, 10, 0.75f);
        LootPool.Builder t2 = GenerateEntityCoinPool(ConfigItemTier.T2, 1, 5, 0.5f);
        LootPool.Builder t3 = GenerateEntityCoinPool(ConfigItemTier.T3, 1, 5, 0.25f);
        LootPool.Builder t4 = GenerateEntityCoinPool(ConfigItemTier.T4, 1, 3, 0.1f);
        LootPool.Builder t5 = GenerateEntityCoinPool(ConfigItemTier.T5, 1, 3, 0.05f);
        LootPool.Builder t6 = GenerateEntityCoinPool(ConfigItemTier.T6, 1, 3, 0.025F);

        //And now combine the pools and save
        consumer.accept(LCLootTables.ENTITY_DROPS_T1, LootTable.lootTable().setParamSet(LootContextParamSets.ENTITY).withPool(t1));
        consumer.accept(LCLootTables.ENTITY_DROPS_T2, LootTable.lootTable().withPool(t1).withPool(t2));
        consumer.accept(LCLootTables.ENTITY_DROPS_T3, LootTable.lootTable().withPool(t1).withPool(t2).withPool(t3));
        consumer.accept(LCLootTables.ENTITY_DROPS_T4, LootTable.lootTable().withPool(t1).withPool(t2).withPool(t3).withPool(t4));
        consumer.accept(LCLootTables.ENTITY_DROPS_T5, LootTable.lootTable().withPool(t1).withPool(t2).withPool(t3).withPool(t4).withPool(t5));
        consumer.accept(LCLootTables.ENTITY_DROPS_T6, LootTable.lootTable().withPool(t1).withPool(t2).withPool(t3).withPool(t4).withPool(t5).withPool(t6));

        //Generate and save Boss Tables
        consumer.accept(LCLootTables.BOSS_DROPS_T1, LootTable.lootTable().withPool(BossPool(ConfigItemTier.T1)));
        consumer.accept(LCLootTables.BOSS_DROPS_T2, LootTable.lootTable().withPool(BossPool(ConfigItemTier.T1)).withPool(BossPool(ConfigItemTier.T2)));
        consumer.accept(LCLootTables.BOSS_DROPS_T3, LootTable.lootTable().withPool(BossPool(ConfigItemTier.T1)).withPool(BossPool(ConfigItemTier.T2)).withPool(BossPool(ConfigItemTier.T3)));
        consumer.accept(LCLootTables.BOSS_DROPS_T4, LootTable.lootTable().withPool(BossPool(ConfigItemTier.T1)).withPool(BossPool(ConfigItemTier.T2)).withPool(BossPool(ConfigItemTier.T3)).withPool(BossPool(ConfigItemTier.T4)));
        consumer.accept(LCLootTables.BOSS_DROPS_T5, LootTable.lootTable().withPool(BossPool(ConfigItemTier.T1)).withPool(BossPool(ConfigItemTier.T2)).withPool(BossPool(ConfigItemTier.T3)).withPool(BossPool(ConfigItemTier.T4)).withPool(BossPool(ConfigItemTier.T5)));
        consumer.accept(LCLootTables.BOSS_DROPS_T6, LootTable.lootTable().withPool(BossPool(ConfigItemTier.T1)).withPool(BossPool(ConfigItemTier.T2)).withPool(BossPool(ConfigItemTier.T3)).withPool(BossPool(ConfigItemTier.T4)).withPool(BossPool(ConfigItemTier.T5)).withPool(GenerateEntityCoinPool(ConfigItemTier.T6, 1, 5, 1.0f)));

    }

    private static LootPool.Builder BossPool(ConfigItemTier tier) { return GenerateEntityCoinPool(tier, 10, 30, 1.0f); }

    private static LootPool.Builder GenerateEntityCoinPool(ConfigItemTier tier, float min, float max, float chance)
    {

        LootPool.Builder lootPoolBuilder = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(ConfigLoot.lootTableTier(tier).apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max))).apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0f, 1f))));

        //Add a random chance to the loot
        if(chance < 1.0f) //Looting Modifier = 0.01f
            lootPoolBuilder.when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(chance, 0.01f));

        return lootPoolBuilder;

    }

}