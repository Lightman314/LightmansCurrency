package io.github.lightman314.lightmanscurrency.datagen.common.loot;

import io.github.lightman314.lightmanscurrency.common.loot.LCLootTables;
import io.github.lightman314.lightmanscurrency.common.loot.LootManager;
import io.github.lightman314.lightmanscurrency.datagen.common.loot.packs.*;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableProvider.SubProviderEntry;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import javax.annotation.Nonnull;
import java.util.List;

public class LCLootTableProvider {

    public static LootTableProvider create(@Nonnull PackOutput output) { return new LootTableProvider(output, LCLootTables.all(),
            List.of(new SubProviderEntry(EntityAddonLoot::new, LootManager.ENTITY_PARAMS),
                    new SubProviderEntry(ChestAddonLoot::new, LootContextParamSets.EMPTY),
                    new SubProviderEntry(BlockDropLoot::new, LootContextParamSets.BLOCK))); }
}
