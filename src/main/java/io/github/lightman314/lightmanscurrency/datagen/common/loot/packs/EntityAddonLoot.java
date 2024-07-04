package io.github.lightman314.lightmanscurrency.datagen.common.loot.packs;

import io.github.lightman314.lightmanscurrency.common.loot.ConfigItemTier;
import io.github.lightman314.lightmanscurrency.common.loot.LCLootTables;
import io.github.lightman314.lightmanscurrency.common.loot.entries.ConfigLoot;
import io.github.lightman314.lightmanscurrency.datagen.common.loot.SimpleSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class EntityAddonLoot extends SimpleSubProvider {

    public EntityAddonLoot(HolderLookup.Provider lookup) { super(lookup); }

    @Override
    public void generate() {

        //Generate default entity pools
        LootPool.Builder t1 = GenerateEntityCoinPool(ConfigItemTier.T1, 1, 10, 0.75f,"T1");
        LootPool.Builder t2 = GenerateEntityCoinPool(ConfigItemTier.T2, 1, 5, 0.5f,"T2");
        LootPool.Builder t3 = GenerateEntityCoinPool(ConfigItemTier.T3, 1, 5, 0.25f,"T3");
        LootPool.Builder t4 = GenerateEntityCoinPool(ConfigItemTier.T4, 1, 3, 0.1f,"T4");
        LootPool.Builder t5 = GenerateEntityCoinPool(ConfigItemTier.T5, 1, 3, 0.05f,"T5");
        LootPool.Builder t6 = GenerateEntityCoinPool(ConfigItemTier.T6, 1, 3, 0.025F,"T6");

        //And now combine the pools and save
        this.register(LCLootTables.ENTITY_DROPS_T1, LootTable.lootTable().setParamSet(LootContextParamSets.ENTITY).withPool(t1));
        this.register(LCLootTables.ENTITY_DROPS_T2, LootTable.lootTable().withPool(t1).withPool(t2));
        this.register(LCLootTables.ENTITY_DROPS_T3, LootTable.lootTable().withPool(t1).withPool(t2).withPool(t3));
        this.register(LCLootTables.ENTITY_DROPS_T4, LootTable.lootTable().withPool(t1).withPool(t2).withPool(t3).withPool(t4));
        this.register(LCLootTables.ENTITY_DROPS_T5, LootTable.lootTable().withPool(t1).withPool(t2).withPool(t3).withPool(t4).withPool(t5));
        this.register(LCLootTables.ENTITY_DROPS_T6, LootTable.lootTable().withPool(t1).withPool(t2).withPool(t3).withPool(t4).withPool(t5).withPool(t6));

        //Generate default boss pools
        t1 = BossPool(ConfigItemTier.T1);
        t2 = BossPool(ConfigItemTier.T2);
        t3 = BossPool(ConfigItemTier.T3);
        t4 = BossPool(ConfigItemTier.T4);
        t5 = BossPool(ConfigItemTier.T5);
        t6 = GenerateEntityCoinPool(ConfigItemTier.T6, 1, 5, 1.0f,"BossT6");

        //Generate and saveItem Boss Tables
        this.register(LCLootTables.BOSS_DROPS_T1, LootTable.lootTable().withPool(t1));
        this.register(LCLootTables.BOSS_DROPS_T2, LootTable.lootTable().withPool(t1).withPool(t2));
        this.register(LCLootTables.BOSS_DROPS_T3, LootTable.lootTable().withPool(t1).withPool(t2).withPool(t3));
        this.register(LCLootTables.BOSS_DROPS_T4, LootTable.lootTable().withPool(t1).withPool(t2).withPool(t3).withPool(t4));
        this.register(LCLootTables.BOSS_DROPS_T5, LootTable.lootTable().withPool(t1).withPool(t2).withPool(t3).withPool(t4).withPool(t5));
        this.register(LCLootTables.BOSS_DROPS_T6, LootTable.lootTable().withPool(t1).withPool(t2).withPool(t3).withPool(t4).withPool(t5).withPool(t6));

    }

    private LootPool.Builder BossPool(ConfigItemTier tier) { return GenerateEntityCoinPool(tier, 10, 30, 1.0f,"Boss" + tier.name()); }

    private LootPool.Builder GenerateEntityCoinPool(ConfigItemTier tier, float min, float max, float chance, String name)
    {

        LootPool.Builder builder = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(ConfigLoot.lootTableTier(tier).apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max))).apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.lookup, UniformGenerator.between(0f, 1f))));


        //Add a random chance to the loot
        if(chance < 1.0f) //Looting Modifier = 0.01f
            builder.when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(this.lookup, chance, 0.01f));

        return builder;

    }

}
