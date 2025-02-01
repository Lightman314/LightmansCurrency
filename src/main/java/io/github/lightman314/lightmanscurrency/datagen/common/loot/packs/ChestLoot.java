package io.github.lightman314.lightmanscurrency.datagen.common.loot.packs;

import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.common.loot.LCLootTables;
import io.github.lightman314.lightmanscurrency.common.loot.entries.AncientCoinLoot;
import io.github.lightman314.lightmanscurrency.common.loot.entries.VanillaLootTableReference;
import io.github.lightman314.lightmanscurrency.datagen.common.loot.LootTableProviderTemplate;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.registries.ForgeRegistries;

public class ChestLoot extends LootTableProviderTemplate {

    @Override
    protected void generateLootTables() {
        this.define(LCLootTables.CHEST_VILLAGE_IDAS_TAIGA_LARGE_BANK,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(LootItem.lootTableItem(ForgeRegistries.ITEMS.getValue(VersionUtil.modResource("quark","abacus"))))
                        ));
        this.define(LCLootTables.CHEST_VILLAGE_IDAS_TAIGA_LARGE_BANK_VAULT,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(EmptyLootItem.emptyItem().setWeight(100))
                                //Villager Gifts
                                .add(VanillaLootTableReference.lootTableReference(BuiltInLootTables.ARMORER_GIFT).setWeight(10))
                                .add(VanillaLootTableReference.lootTableReference(BuiltInLootTables.BUTCHER_GIFT).setWeight(10))
                                .add(VanillaLootTableReference.lootTableReference(BuiltInLootTables.CARTOGRAPHER_GIFT).setWeight(10))
                                .add(VanillaLootTableReference.lootTableReference(BuiltInLootTables.CLERIC_GIFT).setWeight(10))
                                .add(VanillaLootTableReference.lootTableReference(BuiltInLootTables.FARMER_GIFT).setWeight(10))
                                .add(VanillaLootTableReference.lootTableReference(BuiltInLootTables.FISHERMAN_GIFT).setWeight(10))
                                .add(VanillaLootTableReference.lootTableReference(BuiltInLootTables.FLETCHER_GIFT).setWeight(10))
                                .add(VanillaLootTableReference.lootTableReference(BuiltInLootTables.LEATHERWORKER_GIFT).setWeight(10))
                                .add(VanillaLootTableReference.lootTableReference(BuiltInLootTables.LIBRARIAN_GIFT).setWeight(10))
                                .add(VanillaLootTableReference.lootTableReference(BuiltInLootTables.MASON_GIFT).setWeight(10))
                                .add(VanillaLootTableReference.lootTableReference(BuiltInLootTables.SHEPHERD_GIFT).setWeight(10))
                                .add(VanillaLootTableReference.lootTableReference(BuiltInLootTables.TOOLSMITH_GIFT).setWeight(10))
                                .add(VanillaLootTableReference.lootTableReference(BuiltInLootTables.WEAPONSMITH_GIFT).setWeight(10))
                                .add(VanillaLootTableReference.lootTableReference(BuiltInLootTables.CAT_MORNING_GIFT).setWeight(10))
                                //Various Chest Loot
                                .add(VanillaLootTableReference.lootTableReference(BuiltInLootTables.SPAWN_BONUS_CHEST).setWeight(10))
                                //Money (of course)
                                .add(LootTableReference.lootTableReference(LCLootTables.CHEST_DROPS_T1).setWeight(10))
                                .add(LootTableReference.lootTableReference(LCLootTables.CHEST_DROPS_T2).setWeight(8))
                                .add(LootTableReference.lootTableReference(LCLootTables.CHEST_DROPS_T3).setWeight(6))
                                .add(LootTableReference.lootTableReference(LCLootTables.CHEST_DROPS_T4).setWeight(4))
                                .add(LootTableReference.lootTableReference(LCLootTables.CHEST_DROPS_T5).setWeight(2))
                                .add(LootTableReference.lootTableReference(LCLootTables.CHEST_DROPS_T6))
                                //Ancient Coins
                                .add(AncientCoinLoot.ancientCoin(AncientCoinType.COPPER))
                                .add(AncientCoinLoot.ancientCoin(AncientCoinType.IRON))
                                .add(AncientCoinLoot.ancientCoin(AncientCoinType.GOLD))
                                .add(AncientCoinLoot.ancientCoin(AncientCoinType.EMERALD))
                                .add(AncientCoinLoot.ancientCoin(AncientCoinType.DIAMOND))
                                .add(LootTableReference.lootTableReference(LCLootTables.MISC_ANCIENT_NETHERITE_COINS))
                                .add(AncientCoinLoot.ancientCoin(AncientCoinType.LAPIS))
                                .add(AncientCoinLoot.ancientCoin(AncientCoinType.ENDER_PEARL))
                                //4 rolls per chest as each table/entry given other than money has 1 resulting item
                                .setRolls(ConstantValue.exactly(4f))
                        ));
    }
}
