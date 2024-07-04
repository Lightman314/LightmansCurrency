package io.github.lightman314.lightmanscurrency.datagen.common.crafting.builders;

import io.github.lightman314.lightmanscurrency.common.crafting.CoinMintRecipe;
import io.github.lightman314.lightmanscurrency.common.crafting.CoinMintRecipe.MintType;
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

    private final MintType type;
    private final Item result;
    private final int count;
    private int duration = 0;
    private Ingredient ingredient = null;
    private int ingredientCount = 1;

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

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
    public MintRecipeBuilder accepts(@Nonnull Ingredient ingredient, int count) { this.ingredient = ingredient; this.ingredientCount = count; return this; }

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
        consumer.accept(id, new CoinMintRecipe(this.type, this.duration, this.ingredient, this.ingredientCount, new ItemStack(this.result,this.count)), advancement$builder.build(id.withPrefix("recipes/coin_mint/")));
    }

    private void ensureValid(ResourceLocation id) {
        if(this.criteria.isEmpty())
            throw new IllegalStateException("No way of obtaining recipe " + id);
        if(this.ingredient == null)
            throw new IllegalStateException("No ingredient defined for " + id);
    }



}
