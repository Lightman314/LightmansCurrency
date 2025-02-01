package io.github.lightman314.lightmanscurrency.datagen.common.loot.packs;

import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.common.loot.LCLootTables;
import io.github.lightman314.lightmanscurrency.common.loot.entries.AncientCoinLoot;
import io.github.lightman314.lightmanscurrency.datagen.common.loot.LootTableProviderTemplate;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.registries.ForgeRegistries;

public class ArcheologyLoot extends LootTableProviderTemplate {

    @Override
    protected void generateLootTables() {

        //Desert Banker House
        //Emerald Coins
        this.define(LCLootTables.ARCHAEOLOGY_VILLAGE_DESERT_BANKER,
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
        this.define(LCLootTables.ARCHAEOLOGY_VILLAGE_PLAINS_SHOP,
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
        this.define(LCLootTables.ARCHAEOLOGY_VILLAGE_TAIGA_SHOP,
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
        this.define(LCLootTables.ARCHAEOLOGY_VILLAGE_DESERT_SHOP,
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
        this.define(LCLootTables.ARCHAEOLOGY_ANCIENT_RUINS,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(AncientCoinLoot.ancientCoin(AncientCoinType.DIAMOND))
                        ));

        //IDAS
        this.define(LCLootTables.ARCHAEOLOGY_VILLAGE_IDAS_TAIGA_LARGE_BANK,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(AncientCoinLoot.ancientCoin(AncientCoinType.EMERALD).setWeight(3))
                                .add(LootItem.lootTableItem(Items.EMERALD).setWeight(2))
                                .add(LootItem.lootTableItem(ModItems.COIN_EMERALD.get()).setWeight(2))
                                .add(AncientCoinLoot.ancientCoin(AncientCoinType.GOLD).setWeight(2))
                                .add(LootItem.lootTableItem(ModItems.COIN_GOLD.get()).setWeight(1))
                                .add(AncientCoinLoot.ancientCoin(AncientCoinType.IRON).setWeight(2))
                                .add(LootItem.lootTableItem(ModItems.COIN_IRON.get()).setWeight(1))
                                //Reference the bank loot table directly so that this loot table will still parse even if the abacus module isn't loaded
                                .add(LootTableReference.lootTableReference(LCLootTables.CHEST_VILLAGE_IDAS_TAIGA_LARGE_BANK))
                        ));

    }

}