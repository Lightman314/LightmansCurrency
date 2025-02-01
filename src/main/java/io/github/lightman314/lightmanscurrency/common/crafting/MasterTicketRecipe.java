package io.github.lightman314.lightmanscurrency.common.crafting;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.common.core.ModRecipes;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.data.types.TicketDataCache;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.menus.slots.ticket.TicketModifierSlot;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class MasterTicketRecipe implements TicketStationRecipe {

    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final Item result;

    public MasterTicketRecipe(@Nonnull ResourceLocation id, @Nonnull Ingredient ingredient, @Nonnull Item result)
    {
        this.id = id;
        this.ingredient = ingredient;
        this.result = result;
    }

    @Override
    public boolean consumeModifier() { return true; }
    @Nonnull
    @Override
    public List<ItemStack> jeiModifierList() { return TicketStationRecipe.exampleModifierList(Tags.Items.DYES, Items.AIR); }
    @Nonnull
    @Override
    public Ingredient getIngredient() { return this.ingredient; }
    @Nonnull
    @Override
    public ItemStack exampleResult() { return TicketItem.CreateTicket(this.result, -1, 0xFFFF00); }

    @Override
    public boolean validModifier(@Nonnull ItemStack stack) { return stack.isEmpty() || stack.is(Tags.Items.DYES); }
    @Override
    public boolean validIngredient(@Nonnull ItemStack stack) { return this.ingredient.test(stack); }

    @Nonnull
    @Override
    public ItemStack assemble(@Nonnull Container container, @Nonnull RegistryAccess registryAccess) {
        long nextTicketID = TicketDataCache.TYPE.get(false).createNextID();
        ItemStack dyeStack = container.getItem(0);
        Color dyeColor = TicketModifierSlot.getColorFromDye(dyeStack);
        int color = dyeColor == null ? TicketItem.GetDefaultTicketColor(nextTicketID) : dyeColor.hexColor;
        return TicketItem.CreateTicket(this.result, nextTicketID, color, 1);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return true; }

    @Nonnull
    @Override
    public ItemStack getResultItem(@Nonnull RegistryAccess registryAccess) {
        long nextTicketID = TicketDataCache.TYPE.getUnknown().peekNextID();
        int color = TicketItem.GetDefaultTicketColor(nextTicketID);
        return TicketItem.CreateTicket(this.result, nextTicketID, color, 1);
    }

    @Nonnull
    @Override
    public ItemStack peekAtResult(@Nonnull Container container) {
        long nextTicketID = TicketDataCache.TYPE.getUnknown().peekNextID();
        ItemStack dyeStack = container.getItem(0);
        Color dyeColor = TicketModifierSlot.getColorFromDye(dyeStack);
        if(dyeColor != null)
            return TicketItem.CreateTicket(this.result, nextTicketID, dyeColor.hexColor);
        else
            return TicketItem.CreateTicket(this.result, nextTicketID);
    }

    @Nonnull
    @Override
    public ResourceLocation getId() { return this.id; }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() { return ModRecipes.TICKET_MASTER.get(); }

    public static class Serializer implements RecipeSerializer<MasterTicketRecipe>
    {
        @Nonnull
        @Override
        public MasterTicketRecipe fromJson(@Nonnull ResourceLocation id, @Nonnull JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            Item result = CraftingHelper.getItem(GsonHelper.getAsString(json, "result"), true);
            return new MasterTicketRecipe(id, ingredient, result);
        }
        @Override
        @Nullable
        public MasterTicketRecipe fromNetwork(@Nonnull ResourceLocation id, @Nonnull FriendlyByteBuf buffer) {
            return new MasterTicketRecipe(id, Ingredient.fromNetwork(buffer), buffer.readItem().getItem());
        }
        @Override
        public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull MasterTicketRecipe recipe) {
            recipe.ingredient.toNetwork(buffer);
            buffer.writeItem(new ItemStack(recipe.result));
        }
    }

}
