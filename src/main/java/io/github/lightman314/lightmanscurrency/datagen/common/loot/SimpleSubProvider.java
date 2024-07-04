package io.github.lightman314.lightmanscurrency.datagen.common.loot;

import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class SimpleSubProvider implements LootTableSubProvider {

    protected final HolderLookup.Provider lookup;
    public SimpleSubProvider(HolderLookup.Provider lookup) { this.lookup = lookup; }

    private BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer;

    protected LootItemCondition.Builder hasSilkTouch() {
        HolderLookup.RegistryLookup<Enchantment> registrylookup = this.lookup.lookupOrThrow(Registries.ENCHANTMENT);
        return MatchTool.toolMatches(
                ItemPredicate.Builder.item()
                        .withSubPredicate(
                                ItemSubPredicates.ENCHANTMENTS,
                                ItemEnchantmentsPredicate.enchantments(
                                        List.of(new EnchantmentPredicate(registrylookup.getOrThrow(Enchantments.SILK_TOUCH), MinMaxBounds.Ints.atLeast(1)))
                                )
                        )
        );
    }

    protected EnchantedCountIncreaseFunction.Builder addLootingBonus(NumberProvider numberProvider)
    {
        return EnchantedCountIncreaseFunction.lootingMultiplier(this.lookup, numberProvider);
    }

    @Override
    public void generate(@Nonnull BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
        this.consumer = consumer;
        this.generate();
    }

    protected abstract void generate();

    protected final void register(ResourceKey<LootTable> table, LootTable.Builder builder) { this.consumer.accept(table,builder); }
    protected final void register(ResourceLocation table, LootTable.Builder builder) { this.register(ResourceKey.create(Registries.LOOT_TABLE,table), builder);}

}
