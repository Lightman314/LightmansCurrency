package io.github.lightman314.lightmanscurrency.common.crafting;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.common.core.ModRecipes;
import io.github.lightman314.lightmanscurrency.common.crafting.durability.DurabilityData;
import io.github.lightman314.lightmanscurrency.common.crafting.input.TicketStationRecipeInput;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import net.minecraft.MethodsReturnNonnullByDefault;
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
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TicketRecipe implements TicketStationRecipe {

    private final ResourceLocation id;
    private final Ingredient masterIngredient;
    public Ingredient getMasterIngredient() { return this.masterIngredient; }
    private final Ingredient ingredient;

    private final Item ticketResult;
    private final DurabilityData durability;
    public TicketRecipe(ResourceLocation id, Ingredient masterIngredient, Ingredient ingredient, Item result, Optional<DurabilityData> durabilityData) { this(id,masterIngredient,ingredient,result,durabilityData.orElse(DurabilityData.NULL)); }
    public TicketRecipe(ResourceLocation id, Ingredient masterIngredient, Ingredient ingredient, Item result, DurabilityData durability)
    {
        this.id = id;
        this.masterIngredient = masterIngredient;
        this.ingredient = ingredient;
        this.ticketResult = result;
        this.durability = durability;
    }

    @Override
    public List<ItemStack> jeiModifierList() { return TicketStationRecipe.exampleTicketList(this.masterIngredient); }
    @Override
    public Ingredient getIngredient() { return this.ingredient; }
    @Override
    public ItemStack exampleResult() { return new ItemStack(this.ticketResult); }

    @Override
    public boolean consumeModifier() { return false; }
    @Override
    public boolean validModifier(ItemStack stack) { return this.masterIngredient.test(stack); }
    @Override
    public boolean validIngredient(ItemStack stack) { return this.ingredient.test(stack); }

    @Override
    public DurabilityData getDurabilityData() { return this.durability; }

    @Override
    public ItemStack assemble(TicketStationRecipeInput container, RegistryAccess registryAccess) { return this.applyDurability(TicketItem.CraftTicket(container.getItem(0), this.ticketResult),container.data); }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return true; }
    
    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        if(this.masterIngredient.getItems().length == 0)
            return ItemStack.EMPTY;
        return TicketItem.CraftTicket(TicketItem.CreateTicket(this.masterIngredient.getItems()[0].getItem(), -1), this.ticketResult);
    }

    
    @Override
    public ItemStack peekAtResult(Container container, ExtraData data) {
        return this.applyDurability(TicketItem.CraftTicket(container.getItem(0), this.ticketResult),data);
    }

    
    @Override
    public ResourceLocation getId() { return this.id; }

    @Override
    public ItemStack assembleWithKiosk(ItemStack sellItem, ExtraData data) { return this.applyDurability(TicketItem.CraftTicket(sellItem,this.ticketResult),data); }

    private ItemStack applyDurability(ItemStack ticket, ExtraData data)
    {
        if(this.durability.isValid() && data.durability() > 0 && this.durability.test(data.durability()))
            TicketItem.setUseCount(ticket,data.durability());
        return ticket;
    }
    
    @Override
    public RecipeSerializer<?> getSerializer() { return ModRecipes.TICKET.get(); }

    public static class Serializer implements RecipeSerializer<TicketRecipe>
    {
        
        @Override
        public TicketRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient masterIngredient = Ingredient.fromJson(json.get("masterTicket"));
            Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));
            Item item = CraftingHelper.getItem(GsonHelper.getAsString(json,"result"), true);
            Optional<DurabilityData> durability = DurabilityData.parse(json,"durability");
            return new TicketRecipe(id,masterIngredient,ingredient,item,durability);
        }

        @Override
        @Nullable
        public TicketRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new TicketRecipe(id, Ingredient.fromNetwork(buffer), Ingredient.fromNetwork(buffer), buffer.readItem().getItem(),DurabilityData.decode(buffer));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, TicketRecipe recipe) {
            recipe.masterIngredient.toNetwork(buffer);
            recipe.ingredient.toNetwork(buffer);
            buffer.writeItem(new ItemStack(recipe.ticketResult));
            recipe.durability.encode(buffer);
        }
    }

}
