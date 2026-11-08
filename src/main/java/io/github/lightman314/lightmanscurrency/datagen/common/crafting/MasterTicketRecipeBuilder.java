package io.github.lightman314.lightmanscurrency.datagen.common.crafting;

import io.github.lightman314.lightmanscurrency.features.tickets.recipe.MasterTicketRecipe;
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

public class MasterTicketRecipeBuilder implements RecipeBuilder {

    private final HolderGetter<Item> items;
    private String group = "";
    private Ingredient ingredient;
    private final ItemStackTemplate result;

    private final RecipeUnlockAdvancementBuilder advancementBuilder = new RecipeUnlockAdvancementBuilder();

    private MasterTicketRecipeBuilder(HolderGetter<Item> holderGetter,ItemStackTemplate result) {
        this.items = holderGetter;
        this.result = result;
    }

    public static MasterTicketRecipeBuilder of(HolderGetter<Item> itemHolderGetter,ItemLike result) { return of(itemHolderGetter,new ItemStackTemplate(result.asItem())); }
    public static MasterTicketRecipeBuilder of(HolderGetter<Item> itemHolderGetter,ItemStackTemplate result) { return new MasterTicketRecipeBuilder(itemHolderGetter,result); }

    public MasterTicketRecipeBuilder requires(ItemLike... item) { return this.requires(Ingredient.of(item)); }
    public MasterTicketRecipeBuilder requires(TagKey<Item> tag) { return this.requires(Ingredient.of(this.items.getOrThrow(tag))); }
    public MasterTicketRecipeBuilder requires(Ingredient ingredient) { this.ingredient = ingredient; return this; }

    @Override
    public MasterTicketRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.advancementBuilder.unlockedBy(name,criterion);
        return this;
    }

    @Override
    public MasterTicketRecipeBuilder group(@Nullable String group) {
        this.group = group == null ? "" : group;
        return this;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() { return ResourceKey.create(Registries.RECIPE,this.result.item().getKey().identifier().withPrefix("ticket_station/")); }

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> location) {
        if(this.ingredient == null)
            throw new IllegalStateException("Cannot save a Master Ticket recipe without its ingredient defined!");
        output.accept(location,new MasterTicketRecipe(this.group,this.ingredient,this.result),this.advancementBuilder.build(output,location,"ticket_station"));
    }

}
