package io.github.lightman314.lightmanscurrency.datagen.common.loot;

import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;

public abstract class LootTableProviderTemplate implements LootTableSubProvider {

    private BiConsumer<ResourceLocation, LootTable.Builder> consumer = null;

    @Override
    public final void generate(@Nonnull BiConsumer<ResourceLocation, LootTable.Builder> consumer) {
        this.consumer = consumer;
        this.generateLootTables();
    }

    protected abstract void generateLootTables();

    protected final void define(@Nonnull ResourceLocation id, @Nonnull LootTable.Builder lootTable)
    {
        if(this.consumer != null)
            this.consumer.accept(id,lootTable);
    }

}
