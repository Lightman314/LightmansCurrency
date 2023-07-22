package io.github.lightman314.lightmanscurrency.datagen.common.crafting.builders;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.common.core.ModRecipes;
import io.github.lightman314.lightmanscurrency.common.crafting.CoinMintRecipe.MintType;
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

public class MintRecipeBuilder  implements RecipeBuilder {

    private final MintType type;
    private final Item result;
    private final int count;
    private Ingredient ingredient = null;
    private int ingredientCount = 1;

    private final Advancement.Builder advancement = Advancement.Builder.advancement();

    public MintRecipeBuilder(MintType type, ItemLike result, int count) {
        this.type = type;
        this.result = result.asItem();
        this.count = count;
    }

    public static MintRecipeBuilder melt(ItemLike result) { return melt(result, 1); }
    public static MintRecipeBuilder melt(ItemLike result, int count) { return new MintRecipeBuilder(MintType.MELT, result, count); }
    public static MintRecipeBuilder mint(ItemLike result) { return mint(result, 1); }
    public static MintRecipeBuilder mint(ItemLike result, int count) { return new MintRecipeBuilder(MintType.MINT, result, count); }
    public static MintRecipeBuilder other(ItemLike result) { return other(result, 1); }
    public static MintRecipeBuilder other(ItemLike result, int count) { return new MintRecipeBuilder(MintType.OTHER, result, count); }

    @Nonnull
    public MintRecipeBuilder accepts(@Nonnull ItemLike item) { return this.accepts(item, 1); }
    @Nonnull
    public MintRecipeBuilder accepts(@Nonnull ItemLike item, int count) { this.ingredient = Ingredient.of(item); this.ingredientCount = count; return this; }

    @Nonnull
    public MintRecipeBuilder accepts(@Nonnull TagKey<Item> item) { return this.accepts(item, 1); }
    @Nonnull
    public MintRecipeBuilder accepts(@Nonnull TagKey<Item> item, int count) { this.ingredient = Ingredient.of(item); this.ingredientCount = count; return this; }

    @Nonnull
    public MintRecipeBuilder accepts(@Nonnull Ingredient ingredient) { return this.accepts(ingredient, 1); }
    @Nonnull
    public MintRecipeBuilder accepts(@Nonnull Ingredient ingredient, int count) { this.ingredient = ingredient; this.ingredientCount = 1; return this; }

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
        this.advancement.parent(new ResourceLocation("recipes/root")).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id)).rewards(AdvancementRewards.Builder.recipe(id)).requirements(RequirementsStrategy.OR);
        consumer.accept(new Result(id, this.type, this.result, this.count, this.ingredient, this.ingredientCount, this.advancement, AddPrefix(id)));
    }

    private static ResourceLocation AddPrefix(ResourceLocation id) { return new ResourceLocation(id.getNamespace(), "recipes/coin_mint/" + id.getPath()); }

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
        private final MintType type;
        private final Item result;
        private final int count;
        private final Ingredient ingredient;
        private final int ingredientCount;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;

        public Result(ResourceLocation id, MintType type, Item result, int count, Ingredient ingredient, int ingredientCount, Advancement.Builder advancement, ResourceLocation advancementId)
        {
            this.id = id;
            this.type = type;
            this.result = result;
            this.count = count;
            this.ingredient = ingredient;
            this.ingredientCount = ingredientCount;
            this.advancement = advancement;
            this.advancementId = advancementId;
        }

        @Override
        public void serializeRecipeData(@Nonnull JsonObject json) {
            json.addProperty("mintType", this.type.toString());
            json.add("ingredient", this.ingredient.toJson());
            if(this.ingredientCount > 1)
                json.addProperty("count", this.ingredientCount);

            JsonObject resultObject = new JsonObject();
            resultObject.addProperty("item", ForgeRegistries.ITEMS.getKey(this.result).toString());
            if(this.count > 1)
                resultObject.addProperty("count", this.count);
            json.add("result", resultObject);
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