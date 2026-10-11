package io.github.lightman314.lightmanscurrency.datagen.common;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.loot.LCLootTables;
import io.github.lightman314.lightmanscurrency.features.loot.ConfiguredCoinLootModifier;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

import java.util.concurrent.CompletableFuture;

public class LCGlobalLootModifierProvider extends GlobalLootModifierProvider {

    public LCGlobalLootModifierProvider(PackOutput output,CompletableFuture<HolderLookup.Provider> registries) {
        super(output,registries,LCApi.MODID);

    }

    @Override
    protected void start() {
        this.add("coins_in_chest",new ConfiguredCoinLootModifier());
        this.addChestTable(Identifier.withDefaultNamespace("chests/spawn_bonus_chest"),LCLootTables.CHEST_ADDON_BONUS_CHEST);
        this.addChestTable(Identifier.withDefaultNamespace("chests/bastion_treasure"),LCLootTables.CHEST_ADDON_BASTION_TREASURE);
        this.addChestTable(Identifier.withDefaultNamespace("chests/end_city_treasure"),LCLootTables.CHEST_ADDON_END_CITY_TREASURE);
        this.addChestTable(Identifier.withDefaultNamespace("chests/woodland_mansion"),LCLootTables.CHEST_ADDON_WOODLAND_MANSION);
        this.addChestTable(Identifier.withDefaultNamespace("chests/buried_treasure"),LCLootTables.CHEST_ADDON_BURIED_TREASURE);
        this.addChestTable(Identifier.withDefaultNamespace("chests/shipwreck_treasure"),LCLootTables.CHEST_ADDON_SHIPWRECK_TREASURE);
        this.addChestTable(Identifier.withDefaultNamespace("chests/ancient_city"),LCLootTables.CHEST_ADDON_ANCIENT_CITY);
    }

    protected final void addChestTable(Identifier originalTable,Identifier addedTable) {
        String path = originalTable.getPath();
        if(path.startsWith("chests/"))
            path = path.substring("chests/".length());
        this.addChestTable("chest_additions/" + path,originalTable,addedTable);
    }
    protected final void addChestTable(String name,Identifier lootTable,Identifier addedTable) {
        this.add(name,new AddTableLootModifier(
                new LootItemCondition[]{LootTableIdCondition.builder(lootTable).build()},
                0, ResourceKey.create(Registries.LOOT_TABLE,addedTable)));
    }

}
