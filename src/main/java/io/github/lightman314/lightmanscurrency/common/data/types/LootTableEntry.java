package io.github.lightman314.lightmanscurrency.common.data.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public record LootTableEntry(ResourceKey<LootTable> lootTable,long seed) {

    public static final Codec<LootTableEntry> CODEC = RecordCodecBuilder.create(builder ->
        builder.group(ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("lootTable").forGetter(LootTableEntry::lootTable),
                Codec.LONG.fieldOf("seed").forGetter(LootTableEntry::seed))
                .apply(builder,LootTableEntry::new));

    public static final StreamCodec<RegistryFriendlyByteBuf,LootTableEntry> STREAM_CODEC = StreamCodec.of(
            (b,e) -> {
                b.writeResourceKey(e.lootTable);
                b.writeLong(e.seed);
            },
            b -> new LootTableEntry(b.readResourceKey(Registries.LOOT_TABLE),b.readLong())
    );

}
