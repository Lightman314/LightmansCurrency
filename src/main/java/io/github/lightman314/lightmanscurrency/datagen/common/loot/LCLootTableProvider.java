package io.github.lightman314.lightmanscurrency.datagen.common.loot;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.common.loot.LCLootTables;
import io.github.lightman314.lightmanscurrency.common.loot.LootManager;
import io.github.lightman314.lightmanscurrency.datagen.common.loot.packs.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LCLootTableProvider extends LootTableProvider {

    public LCLootTableProvider(@Nonnull DataGenerator output) { super(output); }

    @Nonnull
    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
        return List.of(Pair.of(EntityAddonLoot::new, LootManager.ENTITY_PARAMS),
                Pair.of(ChestAddonLoot::new, LootContextParamSets.EMPTY),
                Pair.of(BlockDropLoot::new, LootContextParamSets.BLOCK));
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, @Nonnull ValidationContext validationtracker) {
        for(ResourceLocation resourcelocation : Sets.difference(LCLootTables.all(), map.keySet())) {
            validationtracker.reportProblem("Missing LightmansCurrency table: " + resourcelocation);
        }

        map.forEach((id, table) -> LootTables.validate(validationtracker, id, table));
    }
}