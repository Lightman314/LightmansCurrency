package io.github.lightman314.lightmanscurrency.datagen.common.crafting.builders;

import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.crafting.CouponRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CouponRecipeBuilder implements RecipeBuilder {

    private final Ingredient ingredient;
    private Item result;

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    private CouponRecipeBuilder(@Nonnull Ingredient ingredient) { this.ingredient = ingredient; }

    public static CouponRecipeBuilder of(@Nonnull TagKey<Item> tag) { return of(Ingredient.of(tag)); }
    public static CouponRecipeBuilder of(@Nonnull ItemLike item) { return of(Ingredient.of(item)); }
    public static CouponRecipeBuilder of(@Nonnull Ingredient ingredient) { return new CouponRecipeBuilder(ingredient); }

    @Nonnull
    public CouponRecipeBuilder withResult(@Nonnull Supplier<? extends ItemLike> result) { return this.withResult(result.get()); }
    public CouponRecipeBuilder withResult(@Nonnull ItemLike result) { this.result = result.asItem(); return this; }

    @Nonnull
    @Override
    public CouponRecipeBuilder unlockedBy(@Nonnull String name, @Nonnull Criterion<?> criteria) { this.criteria.put(name, criteria); return this; }

    @Nonnull
    @Override
    public CouponRecipeBuilder group(@Nullable String group) { return this; }

    @Nonnull
    @Override
    public Item getResult() { return ModItems.TICKET_MASTER.get(); }

    @Override
    public void save(@Nonnull RecipeOutput consumer, @Nonnull ResourceLocation id) {
        this.ensureValid(id);
        Advancement.Builder advancement$builder = consumer.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advancement$builder::addCriterion);
        consumer.accept(id, new CouponRecipe(this.ingredient, this.result),advancement$builder.build(id.withPrefix("recipes/ticket_machine/")));
    }

    private void ensureValid(ResourceLocation id) {
        if(this.criteria.isEmpty())
            throw new IllegalStateException("No way of obtaining recipe " + id);
        if(this.ingredient == null)
            throw new IllegalStateException("No ingredient defined for " + id);
        if(this.result == null)
            throw new IllegalStateException("No result defined for " + id);
    }

}
