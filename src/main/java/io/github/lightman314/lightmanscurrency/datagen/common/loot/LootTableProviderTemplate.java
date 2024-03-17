package io.github.lightman314.lightmanscurrency.datagen.common.loot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class LootTableProviderTemplate implements Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> {

    private BiConsumer<ResourceLocation, LootTable.Builder> consumer = null;

    @Override
    public final void accept(@Nonnull BiConsumer<ResourceLocation, LootTable.Builder> consumer) {
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