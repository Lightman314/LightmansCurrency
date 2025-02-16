package io.github.lightman314.lightmanscurrency.datagen.common.crafting.builders;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.common.core.ModRecipes;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class MintRecipeBuilder implements RecipeBuilder {

    private final Ingredient ingredient;
    private final int ingredientCount;
    private final Item result;
    private final int count;
    private int duration = 0;

    private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();

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
    public MintRecipeBuilder unlockedBy(@Nonnull String name, @Nonnull CriterionTriggerInstance criteria) { this.advancement.addCriterion(name, criteria); return this; }

    @Nonnull
    @Override
    public MintRecipeBuilder group(@Nullable String group) { return this; }

    @Nonnull
    @Override
    public Item getResult() { return this.result; }

    @Override
    public void save(@Nonnull Consumer<FinishedRecipe> consumer, @Nonnull ResourceLocation id) {
        this.ensureValid(id);
        this.advancement.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id)).rewards(AdvancementRewards.Builder.recipe(id)).requirements(RequirementsStrategy.OR);
        consumer.accept(new Result(id, this.result, this.count, this.duration, this.ingredient, this.ingredientCount, this.advancement, id.withPrefix("recipes/coin_mint/")));
    }

    private void ensureValid(ResourceLocation id) {
        if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }
        if(this.ingredient == null)
            throw new IllegalStateException("No ingredient defined for " + id);
    }

    public static class Result implements FinishedRecipe
    {

        private final ResourceLocation id;
        private final Item result;
        private final int count;
        private final int duration;
        private final Ingredient ingredient;
        private final int ingredientCount;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;

        public Result(ResourceLocation id, Item result, int count, int duration, Ingredient ingredient, int ingredientCount, Advancement.Builder advancement, ResourceLocation advancementId)
        {
            this.id = id;
            this.result = result;
            this.count = count;
            this.duration = duration;
            this.ingredient = ingredient;
            this.ingredientCount = ingredientCount;
            this.advancement = advancement;
            this.advancementId = advancementId;
        }

        @Override
        public void serializeRecipeData(@Nonnull JsonObject json) {
            json.add("ingredient", this.ingredient.toJson());
            if(this.ingredientCount > 1)
                json.addProperty("count", this.ingredientCount);

            JsonObject resultObject = new JsonObject();
            resultObject.addProperty("item", ForgeRegistries.ITEMS.getKey(this.result).toString());
            if(this.count > 1)
                resultObject.addProperty("count", this.count);
            json.add("result", resultObject);
            if(this.duration > 0)
                json.addProperty("duration", this.duration);
        }

        @Nonnull
        @Override
        public ResourceLocation getId() { return this.id; }
        @Nonnull
        @Override
        public RecipeSerializer<?> getType() { return ModRecipes.COIN_MINT.get(); }
        @Nullable
        @Override
        public JsonObject serializeAdvancement() { return this.advancement.serializeToJson(); }
        @Nullable
        @Override
        public ResourceLocation getAdvancementId() { return this.advancementId; }

    }

}
