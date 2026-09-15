package io.github.lightman314.lightmanscurrency.datagen.common.crafting;

import io.github.lightman314.lightmanscurrency.features.wallet.crafting.WalletUpgradeRecipe;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WalletUpgradeRecipeBuilder implements RecipeBuilder {

    private final HolderGetter<Item> items;
    private final RecipeCategory category;
    private final Item result;
    private final List<Ingredient> ingredients = new ArrayList<>();
    private final RecipeUnlockAdvancementBuilder advancementBuilder = new RecipeUnlockAdvancementBuilder();
    private @Nullable String group;

    private WalletUpgradeRecipeBuilder(HolderGetter<Item> items, RecipeCategory category,Item result) {
        this.items = items;
        this.category = category;
        this.result = result;
    }

    public static WalletUpgradeRecipeBuilder shapeless(HolderGetter<Item> items,RecipeCategory category,ItemLike item) {
        return new WalletUpgradeRecipeBuilder(items,category,item.asItem());
    }

    public WalletUpgradeRecipeBuilder requires(TagKey<Item> tag) { return this.requires(Ingredient.of(this.items.getOrThrow(tag))); }

    public WalletUpgradeRecipeBuilder requires(ItemLike item) { return this.requires(item,1); }
    public WalletUpgradeRecipeBuilder requires(ItemLike item,int count) { return this.requires(Ingredient.of(item),count); }

    public WalletUpgradeRecipeBuilder requires(Ingredient ingredient) { return this.requires(ingredient,1); }
    public WalletUpgradeRecipeBuilder requires(Ingredient ingredient,int count) {
        for(int i = 0; i < count; ++i)
            this.ingredients.add(ingredient);
        return this;
    }

    @Override
    public WalletUpgradeRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.advancementBuilder.unlockedBy(name,criterion);
        return this;
    }

    @Override
    public WalletUpgradeRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() { return RecipeBuilder.getDefaultRecipeId(new ItemStackTemplate(this.result)); }

    @Override
    public void save(RecipeOutput output,ResourceKey<Recipe<?>> id) {
        WalletUpgradeRecipe recipe = new WalletUpgradeRecipe(RecipeBuilder.createCraftingCommonInfo(true),RecipeBuilder.createCraftingBookInfo(this.category,this.group),this.result,this.ingredients);
        output.accept(id,recipe,this.advancementBuilder.build(output,id,this.category));
    }

}