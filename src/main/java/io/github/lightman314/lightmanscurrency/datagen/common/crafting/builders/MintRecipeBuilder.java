package io.github.lightman314.lightmanscurrency.datagen.common.crafting.builders;

import io.github.lightman314.lightmanscurrency.common.crafting.CoinMintRecipe;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;

public class MintRecipeBuilder implements RecipeBuilder {

    private final Ingredient ingredient;
    private final int ingredientCount;
    private final Item result;
    private final int count;
    private int duration = 0;


    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    private MintRecipeBuilder(@Nonnull Ingredient ingredient, int ingredientCount, @Nonnull ItemLike result, int count) {
        this.ingredient = ingredient;
        this.ingredientCount = ingredientCount;
        this.result = result.asItem();
        this.count = count;
    }

    public static MintRecipeBuilder create(@Nonnull ItemLike input, @Nonnull ItemLike result){ return create(input,1,result,1); }
    public static MintRecipeBuilder create(@Nonnull ItemLike input, int inputCount, @Nonnull ItemLike result){ return create(input,inputCount,result,1); }
    public static MintRecipeBuilder create(@Nonnull ItemLike input, @Nonnull ItemLike result, int resultCount){ return create(input,1,result,resultCount); }
    public static MintRecipeBuilder create(@Nonnull ItemLike input, int inputCount, @Nonnull ItemLike result, int resultCount){ return create(Ingredient.of(input),inputCount,result,resultCount); }

    public static MintRecipeBuilder create(@Nonnull TagKey<Item> input, @Nonnull ItemLike result) { return create(input, 1, result, 1); }
    public static MintRecipeBuilder create(@Nonnull TagKey<Item> input, int inputCount, @Nonnull ItemLike result) { return create(input, inputCount, result, 1); }
    public static MintRecipeBuilder create(@Nonnull TagKey<Item> input, @Nonnull ItemLike result, int resultCount) { return create(input, 1, result, resultCount); }
    public static MintRecipeBuilder create(@Nonnull TagKey<Item> input, int inputCount, @Nonnull ItemLike result, int resultCount) { return create(Ingredient.of(input), inputCount, result, resultCount); }

    public static MintRecipeBuilder create(@Nonnull Ingredient input, @Nonnull ItemLike result) { return create(input, 1, result,1); }
    public static MintRecipeBuilder create(@Nonnull Ingredient input, int inputCount, @Nonnull ItemLike result) { return create(input, inputCount, result,1); }
    public static MintRecipeBuilder create(@Nonnull Ingredient input, @Nonnull ItemLike result, int resultCount) { return create(input, 1, result,resultCount); }
    public static MintRecipeBuilder create(@Nonnull Ingredient input, int inputCount, @Nonnull ItemLike result, int resultCount){ return new MintRecipeBuilder(input,inputCount,result,resultCount); }

    @Nonnull
    public MintRecipeBuilder ofDuration(int duration) { this.duration = duration; return this; }
    @Nonnull
    @Override
    public MintRecipeBuilder unlockedBy(@Nonnull String name, @Nonnull Criterion<?> criteria) { this.criteria.put(name, criteria); return this; }

    @Nonnull
    @Override
    public MintRecipeBuilder group(@Nullable String group) { return this; }

    @Nonnull
    @Override
    public Item getResult() { return this.result; }

    @Override
    public void save(@Nonnull RecipeOutput consumer, @Nonnull ResourceLocation id) {
        this.ensureValid(id);
        Advancement.Builder advancement$builder = consumer.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advancement$builder::addCriterion);
        consumer.accept(id, new CoinMintRecipe(this.duration, this.ingredient, this.ingredientCount, new ItemStack(this.result,this.count)), advancement$builder.build(id.withPrefix("recipes/coin_mint/")));
    }

    private void ensureValid(ResourceLocation id) {
        if(this.criteria.isEmpty())
            throw new IllegalStateException("No way of obtaining recipe " + id);
        if(this.ingredient == null)
            throw new IllegalStateException("No ingredient defined for " + id);
    }

}
