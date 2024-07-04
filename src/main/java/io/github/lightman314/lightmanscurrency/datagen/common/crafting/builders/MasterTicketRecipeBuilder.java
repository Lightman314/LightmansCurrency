package io.github.lightman314.lightmanscurrency.datagen.common.crafting.builders;

import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.crafting.MasterTicketRecipe;
import net.minecraft.advancements.*;
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

public class MasterTicketRecipeBuilder implements RecipeBuilder {

    private final Ingredient ingredient;
    private Item result;

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    private MasterTicketRecipeBuilder(@Nonnull Ingredient ingredient) { this.ingredient = ingredient; }

    public static MasterTicketRecipeBuilder of(@Nonnull TagKey<Item> tag) { return of(Ingredient.of(tag)); }
    public static MasterTicketRecipeBuilder of(@Nonnull ItemLike item) { return of(Ingredient.of(item)); }
    public static MasterTicketRecipeBuilder of(@Nonnull Ingredient ingredient) { return new MasterTicketRecipeBuilder(ingredient); }

    @Nonnull
    public MasterTicketRecipeBuilder withResult(@Nonnull Supplier<? extends ItemLike> result) { return this.withResult(result.get()); }
    public MasterTicketRecipeBuilder withResult(@Nonnull ItemLike result) { this.result = result.asItem(); return this; }

    @Nonnull
    @Override
    public MasterTicketRecipeBuilder unlockedBy(@Nonnull String name, @Nonnull Criterion<?> criteria) { this.criteria.put(name, criteria); return this; }

    @Nonnull
    @Override
    public MasterTicketRecipeBuilder group(@Nullable String group) { return this; }

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
        consumer.accept(id, new MasterTicketRecipe(this.ingredient, this.result),advancement$builder.build(id.withPrefix("recipes/ticket_machine/")));
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
