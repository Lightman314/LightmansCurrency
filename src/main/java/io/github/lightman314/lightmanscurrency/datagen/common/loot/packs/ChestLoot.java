package io.github.lightman314.lightmanscurrency.datagen.common.loot.packs;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.datagen.common.loot.SimpleSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public class ChestLoot extends SimpleSubProvider {

    public ChestLoot(HolderLookup.Provider lookup) { super(lookup); }

    @Override
    protected void generate() {

        this.register(LightmansCurrency.id("chests/additions/bastion_treasure"),
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .when(LootItemRandomChanceCondition.randomChance(0.1f))
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(ModItems.WALLET_NETHERITE.get()))));
        this.register(LightmansCurrency.id("chests/additions/end_city_treasure"),
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .when(LootItemRandomChanceCondition.randomChance(0.1f))
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(ModItems.WALLET_ENDER_DRAGON.get()))));

    }

}
