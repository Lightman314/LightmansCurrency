package io.github.lightman314.lightmanscurrency.common.loot.glm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class BonusItemModifier implements IGlobalLootModifier {

    public static final MapCodec<BonusItemModifier> CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(Codec.STRING.listOf().fieldOf("targets").forGetter(m -> m.targets),
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(m -> m.item),
                    Codec.FLOAT.fieldOf("odds").forGetter(m -> m.odds))
                    .apply(builder, BonusItemModifier::new));

    private final List<String> targets;
    private final Item item;
    private final float odds;
    private BonusItemModifier(@Nonnull List<String> targets, @Nonnull Item item, float odds)
    {
        this.targets = targets;
        this.item = item;
        this.odds = odds;
    }

    @Nonnull
    @Override
    public ObjectArrayList<ItemStack> apply(@Nonnull ObjectArrayList<ItemStack> generatedLoot, @Nonnull LootContext context) {
        String lootTable = context.getQueriedLootTableId().toString();
        if(this.targets.contains(lootTable) && context.getRandom().nextFloat() < this.odds)
            generatedLoot.add(new ItemStack(this.item));
        return generatedLoot;
    }

    @Nonnull
    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() { return CODEC; }

    @Nonnull
    public static Builder builder(@Nonnull Item item, float odds) { return new Builder(item,odds); }

    public static final class Builder
    {
        private final List<String> targets = new ArrayList<>();
        private final Item item;
        private final float odds;

        private Builder(@Nonnull Item item, float odds) { this.item = item; this.odds = odds; }

        @Nonnull
        public Builder withTarget(@Nonnull ResourceKey<LootTable> target) { return this.withTarget(target.location()); }
        @Nonnull
        public Builder withTarget(@Nonnull ResourceLocation target) { return this.withTarget(target.toString()); }
        @Nonnull
        public Builder withTarget(@Nonnull String target) { this.targets.add(target); return this; }
        @Nonnull
        public Builder withTargetKeys(@Nonnull List<ResourceKey<LootTable>> targets) { return this.withTargetIDs(targets.stream().map(ResourceKey::location).toList()); }
        @Nonnull
        public Builder withTargetIDs(@Nonnull List<ResourceLocation> targets) { return this.withTargets(targets.stream().map(ResourceLocation::toString).toList()); }
        @Nonnull
        public Builder withTargets(@Nonnull List<String> targets) { this.targets.addAll(targets); return this; }

        @Nonnull
        public BonusItemModifier build() { return new BonusItemModifier(this.targets, this.item, this.odds); }

    }

}
