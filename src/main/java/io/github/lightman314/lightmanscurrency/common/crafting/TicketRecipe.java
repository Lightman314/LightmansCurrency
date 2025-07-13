package io.github.lightman314.lightmanscurrency.common.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.common.core.ModRecipes;
import io.github.lightman314.lightmanscurrency.common.crafting.input.TicketStationRecipeInput;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nonnull;
import java.util.List;

public class TicketRecipe implements TicketStationRecipe {

    public static final MapCodec<TicketRecipe> CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                    Ingredient.CODEC_NONEMPTY.fieldOf("masterTicket").forGetter(r -> r.masterIngredient),
                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(r -> r.ingredient),
                    ResourceLocation.CODEC.fieldOf("result").forGetter(TicketRecipe::resultID)
            ).apply(builder,TicketRecipe::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf,TicketRecipe> STREAM_CODEC = StreamCodec.of(TicketRecipe::toNetwork,TicketRecipe::fromNetwork);

    private final Ingredient masterIngredient;
    public Ingredient getMasterIngredient() { return this.masterIngredient; }
    private final Ingredient ingredient;

    private final Item ticketResult;
    private ResourceLocation resultID() { return BuiltInRegistries.ITEM.getKey(this.ticketResult); }
    private TicketRecipe(@Nonnull Ingredient masterIngredient, @Nonnull Ingredient ingredient, @Nonnull ResourceLocation resultID) { this(masterIngredient,ingredient, BuiltInRegistries.ITEM.get(resultID)); }
    public TicketRecipe(@Nonnull Ingredient masterIngredient, @Nonnull Ingredient ingredient, @Nonnull Item result)
    {
        this.masterIngredient = masterIngredient;
        this.ingredient = ingredient;
        this.ticketResult = result;
    }

    @Nonnull
    @Override
    public List<ItemStack> jeiModifierList() { return TicketStationRecipe.exampleTicketList(this.masterIngredient); }
    @Nonnull
    @Override
    public Ingredient getIngredient() { return this.ingredient; }
    @Nonnull
    @Override
    public ItemStack exampleResult() { return new ItemStack(this.ticketResult); }

    @Override
    public boolean consumeModifier() { return false; }
    @Override
    public boolean validModifier(@Nonnull ItemStack stack) { return this.masterIngredient.test(stack); }
    @Override
    public boolean validIngredient(@Nonnull ItemStack stack) { return this.ingredient.test(stack); }

    @Nonnull
    @Override
    public ItemStack assemble(@Nonnull TicketStationRecipeInput container, @Nonnull HolderLookup.Provider lookup) {
        return TicketItem.CraftTicket(container.getItem(0), this.ticketResult);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return true; }

    @Nonnull
    @Override
    public ItemStack getResultItem(@Nonnull HolderLookup.Provider registryAccess) {
        if(this.masterIngredient.getItems().length == 0)
            return ItemStack.EMPTY;
        return TicketItem.CraftTicket(TicketItem.CreateTicket(this.masterIngredient.getItems()[0].getItem(), -1), this.ticketResult);
    }

    @Nonnull
    @Override
    public ItemStack peekAtResult(@Nonnull Container container, @Nonnull String code) {
        return TicketItem.CraftTicket(container.getItem(0), this.ticketResult);
    }

    @Override
    public ItemStack assembleWithKiosk(ItemStack sellItem, String code) { return TicketItem.CraftTicket(sellItem,this.ticketResult); }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() { return ModRecipes.TICKET.get(); }

    @Nonnull
    private static TicketRecipe fromNetwork(@Nonnull RegistryFriendlyByteBuf buffer) {
        return new TicketRecipe(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer), Ingredient.CONTENTS_STREAM_CODEC.decode(buffer), ResourceLocation.STREAM_CODEC.decode(buffer));
    }

    private static void toNetwork(@Nonnull RegistryFriendlyByteBuf buffer, @Nonnull TicketRecipe recipe) {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer,recipe.masterIngredient);
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer,recipe.ingredient);
        ResourceLocation.STREAM_CODEC.encode(buffer,recipe.resultID());
    }

    public static class Serializer implements RecipeSerializer<TicketRecipe>
    {

        @Nonnull
        @Override
        public MapCodec<TicketRecipe> codec() { return CODEC; }

        @Nonnull
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TicketRecipe> streamCodec() { return STREAM_CODEC; }
    }

}
