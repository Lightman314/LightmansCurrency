package io.github.lightman314.lightmanscurrency.common.crafting;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.ModRecipes;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class TicketRecipe implements TicketStationRecipe {

    private final ResourceLocation id;
    private final Ingredient ingredient;

    private final Item ticketResult;
    public TicketRecipe(@Nonnull ResourceLocation id, @Nonnull Ingredient ingredient, @Nonnull Item result)
    {
        this.id = id;
        this.ingredient = ingredient;
        this.ticketResult = result;
    }

    @Nonnull
    @Override
    public List<ItemStack> jeiModifierList() { return TicketStationRecipe.exampleTicketList(ModItems.TICKET_MASTER); }
    @Nonnull
    @Override
    public Ingredient getIngredient() { return this.ingredient; }
    @Nonnull
    @Override
    public ItemStack exampleResult() { return new ItemStack(this.ticketResult); }

    @Override
    public boolean consumeModifier() { return false; }
    @Override
    public boolean validModifier(@Nonnull ItemStack stack) { return TicketItem.isMasterTicket(stack); }
    @Override
    public boolean validIngredient(@Nonnull ItemStack stack) { return this.ingredient.test(stack); }

    @Nonnull
    @Override
    public ItemStack assemble(@Nonnull Container container) {
        return TicketItem.CraftTicket(container.getItem(0), this.ticketResult);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return true; }

    @Nonnull
    @Override
    public ItemStack getResultItem() {
        return TicketItem.CraftTicket(TicketItem.CreateMasterTicket(TicketItem.CREATIVE_TICKET_ID, TicketItem.CREATIVE_TICKET_COLOR), this.ticketResult);
    }

    @Nonnull
    @Override
    public ItemStack peekAtResult(@Nonnull Container container) { return TicketItem.CraftTicket(container.getItem(0), this.ticketResult); }

    @Nonnull
    @Override
    public ResourceLocation getId() { return this.id; }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() { return ModRecipes.TICKET.get(); }

    public static class Serializer implements RecipeSerializer<TicketRecipe>
    {
        @Nonnull
        @Override
        public TicketRecipe fromJson(@Nonnull ResourceLocation id, @Nonnull JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));
            Item item = CraftingHelper.getItem(GsonHelper.getAsString(json,"result"), true);
            return new TicketRecipe(id, ingredient, item);
        }

        @Override
        @Nullable
        public TicketRecipe fromNetwork(@Nonnull ResourceLocation id, @Nonnull FriendlyByteBuf buffer) {
            return new TicketRecipe(id, Ingredient.fromNetwork(buffer), buffer.readItem().getItem());
        }

        @Override
        public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull TicketRecipe recipe) {
            recipe.ingredient.toNetwork(buffer);
            buffer.writeItem(new ItemStack(recipe.ticketResult));
        }
    }

}
