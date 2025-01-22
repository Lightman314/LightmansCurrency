package io.github.lightman314.lightmanscurrency.datagen.common.loot.packs;

import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.common.loot.LCLootTables;
import io.github.lightman314.lightmanscurrency.common.loot.entries.AncientCoinLoot;
import io.github.lightman314.lightmanscurrency.datagen.common.loot.SimpleSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;

public class ArcheologyLoot extends SimpleSubProvider {

    public ArcheologyLoot(HolderLookup.Provider lookup) { super(lookup); }

    @Override
    protected void generate() {

        //Desert Banker House
        //Emerald Coins
        this.register(LCLootTables.ARCHAEOLOGY_VILLAGE_DESERT_BANKER,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(AncientCoinLoot.ancientCoin(AncientCoinType.EMERALD).setWeight(1))
                                .add(LootItem.lootTableItem(ModItems.COIN_EMERALD.get()).setWeight(3))
                                .add(LootItem.lootTableItem(Items.EMERALD).setWeight(3))
                                .add(LootItem.lootTableItem(Items.STICK).setWeight(1))
                                .add(LootItem.lootTableItem(Items.MELON_SLICE).setWeight(1))
                                .add(LootItem.lootTableItem(Items.CACTUS).setWeight(1))
                        ));

        //Plains Shop
        //Iron & Emerald Coins
        this.register(LCLootTables.ARCHAEOLOGY_VILLAGE_PLAINS_SHOP,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(AncientCoinLoot.ancientCoin(AncientCoinType.IRON).setWeight(2))
                                .add(LootItem.lootTableItem(ModItems.COIN_IRON.get()).setWeight(3))
                                .add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(2))
                                .add(LootItem.lootTableItem(Items.IRON_NUGGET).setWeight(1))
                                .add(AncientCoinLoot.ancientCoin(AncientCoinType.EMERALD).setWeight(1))
                                .add(LootItem.lootTableItem(ModItems.COIN_EMERALD.get()).setWeight(3))
                                .add(LootItem.lootTableItem(Items.EMERALD).setWeight(3))
                                .add(LootItem.lootTableItem(Items.STICK).setWeight(1))
                                .add(LootItem.lootTableItem(Items.TORCH).setWeight(1))
                                .add(LootItem.lootTableItem(Items.WHEAT).setWeight(1))
                        ));

        //Taiga Shop
        //Iron & Emerald Coins
        this.register(LCLootTables.ARCHAEOLOGY_VILLAGE_TAIGA_SHOP,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(AncientCoinLoot.ancientCoin(AncientCoinType.IRON).setWeight(2))
                                .add(LootItem.lootTableItem(ModItems.COIN_IRON.get()).setWeight(3))
                                .add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(2))
                                .add(LootItem.lootTableItem(Items.IRON_NUGGET).setWeight(1))
                                .add(AncientCoinLoot.ancientCoin(AncientCoinType.EMERALD).setWeight(1))
                                .add(LootItem.lootTableItem(ModItems.COIN_EMERALD.get()).setWeight(3))
                                .add(LootItem.lootTableItem(Items.EMERALD).setWeight(3))
                                .add(LootItem.lootTableItem(Items.STICK).setWeight(1))
                                .add(LootItem.lootTableItem(Items.TORCH).setWeight(1))
                                .add(LootItem.lootTableItem(Items.BEETROOT).setWeight(1))
                        ));

        //Desert Shop
        //Gold & Emerald Coins
        this.register(LCLootTables.ARCHAEOLOGY_VILLAGE_DESERT_SHOP,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(AncientCoinLoot.ancientCoin(AncientCoinType.GOLD).setWeight(2))
                                .add(LootItem.lootTableItem(ModItems.COIN_GOLD.get()).setWeight(3))
                                .add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(2))
                                .add(LootItem.lootTableItem(Items.GOLD_NUGGET).setWeight(1))
                                .add(AncientCoinLoot.ancientCoin(AncientCoinType.EMERALD).setWeight(1))
                                .add(LootItem.lootTableItem(ModItems.COIN_EMERALD.get()).setWeight(3))
                                .add(LootItem.lootTableItem(Items.EMERALD).setWeight(3))
                                .add(LootItem.lootTableItem(Items.STICK).setWeight(1))
                                .add(LootItem.lootTableItem(Items.TORCH).setWeight(1))
                                .add(LootItem.lootTableItem(Items.CACTUS).setWeight(1))
                        ));

        //Ancient City Ruins
        this.register(LCLootTables.ARCHAEOLOGY_ANCIENT_RUINS,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(AncientCoinLoot.ancientCoin(AncientCoinType.DIAMOND))
                        ));

    }

}
