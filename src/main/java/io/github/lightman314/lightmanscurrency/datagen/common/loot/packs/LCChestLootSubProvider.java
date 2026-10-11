package io.github.lightman314.lightmanscurrency.datagen.common.loot.packs;

import io.github.lightman314.lightmanscurrency.api.loot.LCLootTables;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import io.github.lightman314.lightmanscurrency.datagen.common.loot.SimpleSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public class LCChestLootSubProvider extends SimpleSubProvider {

    public LCChestLootSubProvider(HolderLookup.Provider registries) { super(registries); }

    @Override
    protected void generate() {
        this.add(LCLootTables.CHEST_ADDON_BONUS_CHEST,this.singleItemTable(LCItems.WALLET_COPPER,0.75f));
        this.add(LCLootTables.CHEST_ADDON_BASTION_TREASURE,this.singleItemTable(LCItems.WALLET_NETHERITE,0.1f));
        this.add(LCLootTables.CHEST_ADDON_END_CITY_TREASURE,this.singleItemTable(LCItems.WALLET_ENDER_DRAGON,0.1f));
        this.add(LCLootTables.CHEST_ADDON_WOODLAND_MANSION,this.singleItemTable(LCItems.WALLET_EMERALD,0.1f));
        this.add(LCLootTables.CHEST_ADDON_BURIED_TREASURE,this.singleItemTable(LCItems.WALLET_DIAMOND,0.33f));
        this.add(LCLootTables.CHEST_ADDON_SHIPWRECK_TREASURE,this.singleItemTable(LCItems.WALLET_GOLD,0.1f));
        this.add(LCLootTables.CHEST_ADDON_ANCIENT_CITY,this.singleItemTable(LCItems.WALLET_NETHER_STAR,0.01f));
    }

    protected final LootTable.Builder singleItemTable(ItemLike item,float odds) {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .when(LootItemRandomChanceCondition.randomChance(odds))
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(item)));
    }

}