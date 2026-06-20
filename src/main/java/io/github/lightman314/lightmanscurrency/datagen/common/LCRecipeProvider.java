package io.github.lightman314.lightmanscurrency.datagen.common;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.core.LCBlocks;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import io.github.lightman314.lightmanscurrency.api.helpers.ColorHelper;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class LCRecipeProvider extends RecipeProvider {

    protected LCRecipeProvider(HolderLookup.Provider registries,RecipeOutput output) {
        super(registries, output);
    }

    private static class LCRunner extends Runner
    {
        protected LCRunner(PackOutput packOutput,CompletableFuture<HolderLookup.Provider> registries) { super(packOutput, registries); }
        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) { return new LCRecipeProvider(registries,output); }
        @Override
        public String getName() { return "Lightman's Currency Recipes"; }
    }

    public static Runner create(PackOutput output, CompletableFuture<HolderLookup.Provider> registryFuture) { return new LCRunner(output,registryFuture); }

    @Override
    protected void buildRecipes() {

        HolderGetter<Item> lookup = this.registries.lookupOrThrow(Registries.ITEM);

        LCBlocks.DISPLAY_CASE.forEach((color,display) -> {
            ShapedRecipeBuilder.shaped(lookup,RecipeCategory.MISC,display)
                    .unlockedBy("money",MoneyKnowledge())
                    .unlockedBy("trader",TraderKnowledge())
                    .pattern("g").pattern("x").pattern("w")
                    .define('x',LCItems.TRADING_CORE)
                    .define('g', Tags.Items.GLASS_BLOCKS_COLORLESS)
                    .define('w',ColorHelper.getWoolBlock(color))
                    .save(this.output,id("traders/display_case/" + color.toString().toLowerCase(Locale.ENGLISH)));
        });

    }

    protected static Criterion<?> MoneyKnowledge() { return LazyTrigger(LCItems.COIN_COPPER); }
    protected static Criterion<?> TraderKnowledge() { return LazyTrigger(LCBlocks.DISPLAY_CASE.get(DyeColor.WHITE)); }

    protected static Criterion<?> LazyTrigger(ItemLike item) { return InventoryChangeTrigger.TriggerInstance.hasItems(item); }
    protected static Criterion<?> LazyTrigger(HolderGetter<Item> lookup,TagKey<Item> itemTag) { return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(lookup,itemTag)); }

    protected static ResourceKey<Recipe<?>> id(Identifier id) { return ResourceKey.create(Registries.RECIPE,id); }
    protected static ResourceKey<Recipe<?>> id(String path) { return id(LCApi.id(path)); }

}
