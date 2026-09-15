package io.github.lightman314.lightmanscurrency.datagen.common.crafting;

import io.github.lightman314.lightmanscurrency.features.coin_mint.CoinMintRecipe;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeUnlockAdvancementBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class CoinMintRecipeBuilder implements RecipeBuilder {

    private final HolderGetter<Item> items;

    @Nullable
    private String group;
    private Optional<Integer> duration = Optional.empty();
    private Ingredient ingredient;
    private int ingredientCount = 1;
    private final ItemStackTemplate result;

    private final RecipeUnlockAdvancementBuilder advancementBuilder = new RecipeUnlockAdvancementBuilder();

    private CoinMintRecipeBuilder(HolderGetter<Item> items,ItemStackTemplate result) { this.items = items; this.result = result; }

    public static CoinMintRecipeBuilder create(HolderGetter<Item> items,ItemLike result) { return create(items,result,1); }
    public static CoinMintRecipeBuilder create(HolderGetter<Item> items,ItemLike result,int outputCount) { return new CoinMintRecipeBuilder(items,new ItemStackTemplate(result.asItem(),outputCount)); }
    public static CoinMintRecipeBuilder create(HolderGetter<Item> items,ItemStackTemplate result) { return new CoinMintRecipeBuilder(items,result); }

    public CoinMintRecipeBuilder withDuration(int duration) { this.duration = duration <= 0 ? Optional.empty() : Optional.of(duration); return this; }

    public CoinMintRecipeBuilder requires(ItemLike... items) { return this.requires(1,items); }
    public CoinMintRecipeBuilder requires(TagKey<Item> tag) { return this.requires(1,tag); }
    public CoinMintRecipeBuilder requires(Ingredient ingredient) { return this.requires(1,ingredient); }

    public CoinMintRecipeBuilder requires(int count,ItemLike... items) { return this.requires(count,Ingredient.of(items)); }
    public CoinMintRecipeBuilder requires(int count,TagKey<Item> tag) { return this.requires(count,Ingredient.of(this.items.getOrThrow(tag))); }
    public CoinMintRecipeBuilder requires(int count,Ingredient ingredient) { this.ingredient = ingredient; this.ingredientCount = count; return this; }

    @Override
    public CoinMintRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.advancementBuilder.unlockedBy(name,criterion);
        return this;
    }

    @Override
    public CoinMintRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() { return ResourceKey.create(Registries.RECIPE,this.result.typeHolder().getKey().identifier().withPrefix("coin_mint/")); }

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> location) {
        if(this.ingredient == null)
            throw new IllegalStateException("Cannot save a Coin Mint recipe without it's ingredient defined!\n" + location);
        CoinMintRecipe recipe = new CoinMintRecipe(this.group,this.duration,this.ingredient,this.ingredientCount,this.result);
        output.accept(location,recipe,this.advancementBuilder.build(output,location,"coin_mint"));
    }

}
