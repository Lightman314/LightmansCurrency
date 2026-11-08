package io.github.lightman314.lightmanscurrency.datagen.common.crafting;

import io.github.lightman314.lightmanscurrency.features.tickets.recipe.DurabilityData;
import io.github.lightman314.lightmanscurrency.features.tickets.recipe.TicketRecipe;
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

public class TicketRecipeBuilder implements RecipeBuilder {

    private final HolderGetter<Item> items;
    private String group = "";
    private Ingredient masterIngredient;
    private Ingredient ingredient;
    private final ItemStackTemplate result;
    private DurabilityData durabilityData = DurabilityData.DEFAULT;

    private final RecipeUnlockAdvancementBuilder builder = new RecipeUnlockAdvancementBuilder();

    private TicketRecipeBuilder(HolderGetter<Item> items,ItemStackTemplate result) {
        this.items = items;
        this.result = result;
    }

    public static TicketRecipeBuilder of(HolderGetter<Item> items,ItemLike result) { return of(items,new ItemStackTemplate(result.asItem())); }
    public static TicketRecipeBuilder of(HolderGetter<Item> items,ItemStackTemplate result) { return new TicketRecipeBuilder(items,result); }

    public TicketRecipeBuilder withMasterTicket(ItemLike masterTicket) { return this.withMasterTicket(Ingredient.of(masterTicket)); }
    public TicketRecipeBuilder withMasterTicket(TagKey<Item> masterTicket) { return this.withMasterTicket(Ingredient.of(this.items.getOrThrow(masterTicket))); }
    public TicketRecipeBuilder withMasterTicket(Ingredient masterTicket) { this.masterIngredient = masterTicket; return this; }

    public TicketRecipeBuilder withIngredient(ItemLike ingredient) { return this.withIngredient(Ingredient.of(ingredient)); }
    public TicketRecipeBuilder withIngredient(TagKey<Item> ingredient) { return this.withIngredient(Ingredient.of(this.items.getOrThrow(ingredient))); }
    public TicketRecipeBuilder withIngredient(Ingredient ingredient) { this.ingredient = ingredient; return this; }

    public TicketRecipeBuilder withDurabilityRange(int min,int max) { return this.withDurabilityRange(false,min,max); }
    public TicketRecipeBuilder withDurabilityRange(boolean allowInfinite,int min,int max) { this.durabilityData = new DurabilityData(allowInfinite,min,max).assertValid(); return this; }

    @Override
    public TicketRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.builder.unlockedBy(name,criterion);
        return this;
    }

    @Override
    public TicketRecipeBuilder group(@Nullable String group) {
        this.group = group == null ? "" : this.group;
        return this;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return ResourceKey.create(Registries.RECIPE,this.result.item().getKey().identifier().withPrefix("ticket_station/"));
    }

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> location) {
        if(this.ingredient == null || this.masterIngredient == null)
            throw new IllegalStateException("Cannot create TicketRecipe if the ingredient and master ingredient are not both defined!");
        output.accept(location,new TicketRecipe(this.group,this.masterIngredient,this.ingredient,this.result,this.durabilityData),this.builder.build(output,location,"ticket_station"));
    }

}