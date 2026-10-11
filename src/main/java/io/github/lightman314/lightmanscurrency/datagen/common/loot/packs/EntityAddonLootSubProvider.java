package io.github.lightman314.lightmanscurrency.datagen.common.loot.packs;

import io.github.lightman314.lightmanscurrency.api.loot.LCLootTables;
import io.github.lightman314.lightmanscurrency.api.loot.pool.ConfigLoot;
import io.github.lightman314.lightmanscurrency.datagen.common.loot.SimpleSubProvider;
import io.github.lightman314.lightmanscurrency.api.loot.ConfigItemTier;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class EntityAddonLootSubProvider extends SimpleSubProvider {

    public EntityAddonLootSubProvider(HolderLookup.Provider registries) { super(registries); }

    @Override
    protected void generate() {

        LootPool.Builder t1 = this.generateEntityCoinPool(ConfigItemTier.T1, 10,0.75f);
        LootPool.Builder t2 = generateEntityCoinPool(ConfigItemTier.T2, 5,0.5f);
        LootPool.Builder t3 = generateEntityCoinPool(ConfigItemTier.T3, 5,0.25f);
        LootPool.Builder t4 = generateEntityCoinPool(ConfigItemTier.T4, 3,0.1f);
        LootPool.Builder t5 = generateEntityCoinPool(ConfigItemTier.T5, 3,0.05f);
        LootPool.Builder t6 = generateEntityCoinPool(ConfigItemTier.T5, 3,0.025f);

        this.add(LCLootTables.ENTITY_DROPS_T1, LootTable.lootTable().withPool(t1));
        this.add(LCLootTables.ENTITY_DROPS_T2, LootTable.lootTable().withPool(t1).withPool(t2));
        this.add(LCLootTables.ENTITY_DROPS_T3, LootTable.lootTable().withPool(t1).withPool(t2).withPool(t3));
        this.add(LCLootTables.ENTITY_DROPS_T4, LootTable.lootTable().withPool(t1).withPool(t2).withPool(t3).withPool(t4));
        this.add(LCLootTables.ENTITY_DROPS_T5, LootTable.lootTable().withPool(t1).withPool(t2).withPool(t3).withPool(t4).withPool(t5));
        this.add(LCLootTables.ENTITY_DROPS_T6, LootTable.lootTable().withPool(t1).withPool(t2).withPool(t3).withPool(t4).withPool(t5).withPool(t6));

        t1 = this.generateBossPool(ConfigItemTier.T1);
        t2 = this.generateBossPool(ConfigItemTier.T2);
        t3 = this.generateBossPool(ConfigItemTier.T3);
        t4 = this.generateBossPool(ConfigItemTier.T4);
        t5 = this.generateBossPool(ConfigItemTier.T5);
        t6 = this.generateBossPool(ConfigItemTier.T6);

        this.add(LCLootTables.BOSS_DROPS_T1, LootTable.lootTable().withPool(t1));
        this.add(LCLootTables.BOSS_DROPS_T2, LootTable.lootTable().withPool(t1).withPool(t2));
        this.add(LCLootTables.BOSS_DROPS_T3, LootTable.lootTable().withPool(t1).withPool(t2).withPool(t3));
        this.add(LCLootTables.BOSS_DROPS_T4, LootTable.lootTable().withPool(t1).withPool(t2).withPool(t3).withPool(t4));
        this.add(LCLootTables.BOSS_DROPS_T5, LootTable.lootTable().withPool(t1).withPool(t2).withPool(t3).withPool(t4).withPool(t5));
        this.add(LCLootTables.BOSS_DROPS_T6, LootTable.lootTable().withPool(t1).withPool(t2).withPool(t3).withPool(t4).withPool(t5).withPool(t6));

    }

    protected LootPool.Builder generateBossPool(ConfigItemTier tier) { return this.generateEntityCoinPool(tier,10,30,1,"Boss" + tier.name()); }

    protected LootPool.Builder generateEntityCoinPool(ConfigItemTier tier,float max,float chance) { return this.generateEntityCoinPool(tier, (float) 1,max,chance,tier.name()); }
    protected LootPool.Builder generateEntityCoinPool(ConfigItemTier tier,float min,float max,float chance,String name) {
        LootPoolSingletonContainer.Builder<?> temp = ConfigLoot.lootTableTier(tier).apply(SetItemCountFunction.setCount(UniformGenerator.between(min,max)));
        temp = temp.apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.registries,UniformGenerator.between(0f,1f)));
        LootPool.Builder builder = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(temp);
        //Add a random chance to the loot
        if(chance < 1f)
            builder.when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(this.registries,chance,0.01f));
        return builder;
    }

}
