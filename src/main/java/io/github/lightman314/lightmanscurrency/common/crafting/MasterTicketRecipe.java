package io.github.lightman314.lightmanscurrency.common.crafting;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.common.core.ModRecipes;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.crafting.input.TicketStationRecipeInput;
import io.github.lightman314.lightmanscurrency.common.data.types.TicketDataCache;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.menus.slots.ticket.TicketModifierSlot;
import net.minecraft.MethodsReturnNonnullByDefault;
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

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MasterTicketRecipe implements TicketStationRecipe {

    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final Item result;

    public MasterTicketRecipe(ResourceLocation id, Ingredient ingredient, Item result)
    {
        this.id = id;
        this.ingredient = ingredient;
        this.result = result;
    }

    @Override
    public boolean consumeModifier() { return true; }
    @Override
    public List<ItemStack> jeiModifierList() { return TicketStationRecipe.exampleModifierList(Tags.Items.DYES, Items.AIR); }
    @Override
    public Ingredient getIngredient() { return this.ingredient; }
    @Override
    public ItemStack exampleResult() { return TicketItem.CreateTicket(this.result, -1, 0xFFFF00); }

    @Override
    public boolean validModifier(ItemStack stack) { return stack.isEmpty() || stack.is(Tags.Items.DYES); }
    @Override
    public boolean validIngredient(ItemStack stack) { return this.ingredient.test(stack); }


    @Override
    public ItemStack assemble(TicketStationRecipeInput container, RegistryAccess registryAccess) {
        long nextTicketID = TicketDataCache.TYPE.isLoaded(false) ? TicketDataCache.TYPE.get(false).createNextID() : -100L;
        ItemStack dyeStack = container.getItem(0);
        Color dyeColor = TicketModifierSlot.getColorFromDye(dyeStack);
        int color = dyeColor == null ? TicketItem.GetDefaultTicketColor(nextTicketID) : dyeColor.hexColor;
        return TicketItem.CreateTicket(this.result, nextTicketID, color, 1);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return true; }


    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        long nextTicketID = TicketDataCache.TYPE.getUnknown().peekNextID();
        int color = TicketItem.GetDefaultTicketColor(nextTicketID);
        return TicketItem.CreateTicket(this.result, nextTicketID, color, 1);
    }


    @Override
    public ItemStack peekAtResult(Container container,String code) {
        long nextTicketID = TicketDataCache.TYPE.getUnknown().peekNextID();
        ItemStack dyeStack = container.getItem(0);
        Color dyeColor = TicketModifierSlot.getColorFromDye(dyeStack);
        if(dyeColor != null)
            return TicketItem.CreateTicket(this.result, nextTicketID, dyeColor.hexColor);
        else
            return TicketItem.CreateTicket(this.result, nextTicketID);
    }

    @Override
    public ItemStack assembleWithKiosk(ItemStack sellITem, String code) { return ItemStack.EMPTY; }


    @Override
    public ResourceLocation getId() { return this.id; }


    @Override
    public RecipeSerializer<?> getSerializer() { return ModRecipes.TICKET_MASTER.get(); }

    public static class Serializer implements RecipeSerializer<MasterTicketRecipe>
    {

        @Override
        public MasterTicketRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            Item result = CraftingHelper.getItem(GsonHelper.getAsString(json, "result"), true);
            return new MasterTicketRecipe(id, ingredient, result);
        }
        @Override
        @Nullable
        public MasterTicketRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new MasterTicketRecipe(id, Ingredient.fromNetwork(buffer), buffer.readItem().getItem());
        }
        @Override
        public void toNetwork(FriendlyByteBuf buffer, MasterTicketRecipe recipe) {
            recipe.ingredient.toNetwork(buffer);
            buffer.writeItem(new ItemStack(recipe.result));
        }
    }

}
