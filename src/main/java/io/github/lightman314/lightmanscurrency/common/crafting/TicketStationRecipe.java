package io.github.lightman314.lightmanscurrency.common.crafting;

import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.crafting.input.ListRecipeInput;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public interface TicketStationRecipe extends Recipe<ListRecipeInput> {

    @Nonnull
    @Override
    default RecipeType<TicketStationRecipe> getType() { return RecipeTypes.TICKET.get(); }

    @Nonnull
    @Override
    default ItemStack getToastSymbol() { return new ItemStack(ModBlocks.TICKET_STATION.get()); }

    @Nonnull
    static List<ItemStack> exampleModifierList(@Nonnull TagKey<Item> tag, @Nonnull Item... extra)
    {
        List<ItemStack> result = new ArrayList<>();
        for(Item extraItem : extra)
            result.add(new ItemStack(extraItem));
        for(HolderSet<Item> set : BuiltInRegistries.ITEM.getTag(tag).stream().toList())
        {
            for(Holder<Item> item : set.stream().toList())
            {
                result.add(new ItemStack(item.value()));
            }
        }
        return result;
    }

    @Nonnull
    static List<ItemStack> exampleTicketList(@Nonnull Supplier<? extends ItemLike> item) { return exampleTicketList(item.get().asItem()); }
    @Nonnull
    static List<ItemStack> exampleTicketList(@Nonnull Ingredient ingredient)
    {
        List<ItemStack> result = new ArrayList<>();
        for(ItemStack item : ingredient.getItems())
            result.addAll(exampleTicketList(item.getItem()));
        return result;
    }
    static List<ItemStack> exampleTicketList(@Nonnull Item item)
    {
        List<ItemStack> result = new ArrayList<>();
        for(Color color : Color.values())
            result.add(TicketItem.CreateExampleTicket(item, color));
        return result;
    }

    boolean consumeModifier();

    boolean validModifier(@Nonnull ItemStack stack);
    @Nonnull
    List<ItemStack> jeiModifierList();
    boolean validIngredient(@Nonnull ItemStack stack);
    @Nonnull
    Ingredient getIngredient();

    @Nonnull
    ItemStack peekAtResult(@Nonnull Container container);
    @Nonnull
    ItemStack exampleResult();

    @Override
    default boolean matches(@Nonnull ListRecipeInput container, @Nonnull Level level) { return this.validModifier(container.getItem(0)) && this.validIngredient(container.getItem(1)); }

}
