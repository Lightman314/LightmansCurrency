package io.github.lightman314.lightmanscurrency.datagen.common.crafting.builders;

import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.crafting.TicketRecipe;
import io.github.lightman314.lightmanscurrency.common.crafting.durability.DurabilityData;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public class TicketRecipeBuilder implements RecipeBuilder {

    private Ingredient masterIngredient;
    private final Ingredient ingredient;
    private final Item result;
    private DurabilityData durability = DurabilityData.NULL;

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    private TicketRecipeBuilder(@Nonnull Ingredient ingredient, @Nonnull ItemLike result) { this.ingredient = ingredient; this.result = result.asItem(); }

    public static TicketRecipeBuilder of(@Nonnull TagKey<Item> tag, @Nonnull ItemLike result) { return of(Ingredient.of(tag), result); }
    public static TicketRecipeBuilder of(@Nonnull ItemLike item, @Nonnull ItemLike result) { return of(Ingredient.of(item), result); }
    public static TicketRecipeBuilder of(@Nonnull Ingredient ingredient, @Nonnull ItemLike result) { return new TicketRecipeBuilder(ingredient, result); }

    @Nonnull
    public TicketRecipeBuilder withMasterTicket(@Nonnull TagKey<Item> tag) { this.masterIngredient = Ingredient.of(tag); return this; }
    @Nonnull
    public TicketRecipeBuilder withMasterTicket(@Nonnull ItemLike item) { this.masterIngredient = Ingredient.of(item); return this; }
    @Nonnull
    public TicketRecipeBuilder withMasterTicket(@Nonnull Ingredient ingredient) { this.masterIngredient = ingredient; return this; }
    @Nonnull
    public TicketRecipeBuilder withDurabilityRange(int min, int max) { return this.withDurabilityRange(false,min,max); }
    public TicketRecipeBuilder withDurabilityRange(boolean allowInfinite,int min, int max) { this.durability = new DurabilityData(allowInfinite,min,max); return this; }

    @Nonnull
    @Override
    public TicketRecipeBuilder unlockedBy(@Nonnull String name, @Nonnull Criterion<?> criteria) { this.criteria.put(name, criteria); return this; }

    @Nonnull
    @Override
    public TicketRecipeBuilder group(@Nullable String group) { return this; }

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
        consumer.accept(id, new TicketRecipe(this.masterIngredient,this.ingredient,this.result,this.durability), advancement$builder.build(id.withPrefix("recipes/ticket_machine/")));
    }

    private void ensureValid(ResourceLocation id) {
        if(this.criteria.isEmpty())
            throw new IllegalStateException("No way of obtaining recipe " + id);
        if(this.ingredient == null)
            throw new IllegalStateException("No ingredient defined for " + id);
        if(this.masterIngredient == null)
            throw new IllegalStateException("No master ticket defined for " + id);
    }

}