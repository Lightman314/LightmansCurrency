package io.github.lightman314.lightmanscurrency.common.crafting;

import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public interface TicketStationRecipe extends Recipe<Container> {

    @Nonnull
    @Override
    default RecipeType<TicketStationRecipe> getType() { return RecipeTypes.TICKET.get(); }

    @Nonnull
    static List<ItemStack> exampleModifierList(@Nonnull TagKey<Item> tag, @Nonnull Item... extra)
    {
        List<ItemStack> result = new ArrayList<>();
        for(Item extraItem : extra)
            result.add(new ItemStack(extraItem));
        for(Item modifier : ForgeRegistries.ITEMS.tags().getTag(tag))
            result.add(new ItemStack(modifier));
        return result;
    }

    @Nonnull
    static List<ItemStack> exampleTicketList(@Nonnull RegistryObject<? extends ItemLike> item) { return exampleTicketList(item.get().asItem()); }
    @Nonnull
    static List<ItemStack> exampleTicketList(@Nonnull Ingredient ingredient)
    {
        List<ItemStack> result = new ArrayList<>();
        for(ItemStack item : ingredient.getItems())
            result.addAll(exampleTicketList(item.getItem()));
        return result;
    }
    @Nonnull
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
    default boolean matches(@Nonnull Container container, @Nonnull Level level) { return this.validModifier(container.getItem(0)) && this.validIngredient(container.getItem(1)); }

}
