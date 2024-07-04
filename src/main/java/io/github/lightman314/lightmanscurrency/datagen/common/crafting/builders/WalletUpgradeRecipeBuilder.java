package io.github.lightman314.lightmanscurrency.datagen.common.crafting.builders;

import io.github.lightman314.lightmanscurrency.common.crafting.WalletUpgradeRecipe;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public class WalletUpgradeRecipeBuilder implements RecipeBuilder {

    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private final NonNullList<Ingredient> ingredients = NonNullList.create();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;

    public WalletUpgradeRecipeBuilder(RecipeCategory category, ItemLike result, int count) {
        this.category = category;
        this.result = result.asItem();
        this.count = count;
    }

    public static WalletUpgradeRecipeBuilder shapeless(RecipeCategory category, ItemLike result) {
        return new WalletUpgradeRecipeBuilder(category, result, 1);
    }

    public static WalletUpgradeRecipeBuilder shapeless(RecipeCategory category, ItemLike result, int count) {
        return new WalletUpgradeRecipeBuilder(category, result, count);
    }

    public WalletUpgradeRecipeBuilder requires(TagKey<Item> p_206420_) {
        return this.requires(Ingredient.of(p_206420_));
    }

    public WalletUpgradeRecipeBuilder requires(ItemLike p_126210_) {
        return this.requires(p_126210_, 1);
    }

    public WalletUpgradeRecipeBuilder requires(ItemLike p_126212_, int p_126213_) {
        for(int i = 0; i < p_126213_; ++i) {
            this.requires(Ingredient.of(p_126212_));
        }

        return this;
    }

    public WalletUpgradeRecipeBuilder requires(Ingredient ingredient) {
        return this.requires(ingredient, 1);
    }

    public WalletUpgradeRecipeBuilder requires(Ingredient ingredient, int count) {
        for(int i = 0; i < count; ++i)
            this.ingredients.add(ingredient);
        return this;
    }

    @Nonnull
    public WalletUpgradeRecipeBuilder unlockedBy(@Nonnull String name, @Nonnull Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Nonnull
    public WalletUpgradeRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

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
        consumer.accept(id, new WalletUpgradeRecipe(this.group == null ? "" : this.group, RecipeBuilder.determineBookCategory(this.category), new ItemStack(this.result, this.count), this.ingredients), advancement$builder.build(id.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private void ensureValid(@Nonnull ResourceLocation id)
    {
        if(this.criteria.isEmpty())
            throw new IllegalStateException("No way of obtaining recipe " + id);
    }

}
