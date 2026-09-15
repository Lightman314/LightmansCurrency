package io.github.lightman314.lightmanscurrency.datagen.common.loot;

import io.github.lightman314.lightmanscurrency.datagen.common.loot.packs.LCBlockLootSubProvider;
import io.github.lightman314.lightmanscurrency.datagen.common.loot.packs.LCFallingBlockLootSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableProvider.SubProviderEntry;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class LCLootTableProvider {

    public static LootTableProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        return new LootTableProvider(output,Set.of(),
                List.of(new SubProviderEntry(LCBlockLootSubProvider::new,LootContextParamSets.BLOCK),
                        new SubProviderEntry(LCFallingBlockLootSubProvider::new,LootContextParamSets.EMPTY)),
                registries);
    }



}