package io.github.lightman314.lightmanscurrency.datagen.common.loot;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.function.BiConsumer;

public abstract class SimpleSubProvider implements LootTableSubProvider {

    protected final HolderLookup.Provider registries;
    public SimpleSubProvider(HolderLookup.Provider registries) { this.registries = registries; }

    private BiConsumer<ResourceKey<LootTable>,LootTable.Builder> output = (k,b) -> { throw new IllegalStateException("Attempted to add an entry to the loot table provider outside of the generate method!"); };

    @Override
    public final void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        this.output = output;
        this.generate();
    }

    protected abstract void generate();

    protected final void add(Identifier table,LootTable.Builder builder) { this.add(ResourceKey.create(Registries.LOOT_TABLE,table),builder); }
    protected final void add(ResourceKey<LootTable> table,LootTable.Builder builder) { this.output.accept(table,builder); }

}
