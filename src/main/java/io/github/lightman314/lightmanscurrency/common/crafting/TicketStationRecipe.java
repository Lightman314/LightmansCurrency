package io.github.lightman314.lightmanscurrency.common.crafting;

import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.crafting.input.TicketStationRecipeInput;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.util.ItemRequirement;
import net.minecraft.core.Holder;
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
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface TicketStationRecipe extends Recipe<TicketStationRecipeInput> {

    Predicate<String> CODE_PREDICATE = s -> {
        if(s.isBlank())
            return false;
        if(s.length() > 16)
            return false;
        for(int i = 0; i < s.length(); ++i)
        {
            if(!validCodeChar(s.charAt(i)))
                return false;
        }
        return true;
    };

    Predicate<String> CODE_INPUT_PREDICATE = s -> s.isEmpty() || CODE_PREDICATE.test(s);

    static boolean validCodeChar(char codeChar) { return codeChar >= 'a' && codeChar <= 'z' || codeChar >= 'A' && codeChar <= 'Z' || codeChar >= '0' && codeChar <= '9'; }

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
        for(Holder<Item> item : BuiltInRegistries.ITEM.getTagOrEmpty(tag))
            result.add(new ItemStack(item.value()));
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
    ItemStack peekAtResult(@Nonnull Container container, @Nonnull String code);
    @Nonnull
    ItemStack exampleResult();

    default boolean requiredCodeInput() { return false; }
    default boolean validCode(String code) { return !this.requiredCodeInput() || CODE_PREDICATE.test(code); }

    //Don't put "valid code" check here, as it will prevent the recipe from being visible as the code input won't appear unless they can first select the code requiring recipe
    @Override
    default boolean matches(@Nonnull TicketStationRecipeInput container, @Nonnull Level level) { return this.validModifier(container.getItem(0)) && this.validIngredient(container.getItem(1)); }

    //Ticket Kiosk Crafting
    default boolean matchesTicketKioskSellItem(ItemStack sellItem) { return !this.consumeModifier() && this.validModifier(sellItem); }
    default boolean allowIgnoreKioskRecipe() { return false; }
    ItemStack assembleWithKiosk(ItemStack sellItem,String code);
    default ItemRequirement getKioskStorageRequirement(ItemStack sellItem) { return ItemRequirement.of(this.getIngredient(),sellItem.getCount()); }

}
