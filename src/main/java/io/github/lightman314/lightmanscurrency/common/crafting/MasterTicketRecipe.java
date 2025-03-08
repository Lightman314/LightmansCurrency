package io.github.lightman314.lightmanscurrency.common.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.common.core.ModRecipes;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.crafting.input.ListRecipeInput;
import io.github.lightman314.lightmanscurrency.common.data.types.TicketDataCache;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.menus.slots.ticket.TicketModifierSlot;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nonnull;
import java.util.List;

public class MasterTicketRecipe implements TicketStationRecipe {

    public static final MapCodec<MasterTicketRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(r -> r.ingredient),
                            ResourceLocation.CODEC.fieldOf("result").forGetter(MasterTicketRecipe::resultID))
                    .apply(builder, MasterTicketRecipe::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf,MasterTicketRecipe> STREAM_CODEC = StreamCodec.of(MasterTicketRecipe::toNetwork,MasterTicketRecipe::fromNetwork);

    private final Ingredient ingredient;
    private final Item result;
    private ResourceLocation resultID() { return BuiltInRegistries.ITEM.getKey(this.result); }

    //Constructor for codec
    private MasterTicketRecipe(@Nonnull Ingredient ingredient, @Nonnull ResourceLocation resultID) { this(ingredient, BuiltInRegistries.ITEM.get(resultID)); }
    public MasterTicketRecipe(@Nonnull Ingredient ingredient, @Nonnull Item result)
    {
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
    public ItemStack assemble(ListRecipeInput container, @Nonnull HolderLookup.Provider lookup) {
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
    public ItemStack getResultItem(@Nonnull HolderLookup.Provider lookup) {
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
    public RecipeSerializer<?> getSerializer() { return ModRecipes.TICKET_MASTER.get(); }

    @Nonnull
    private static MasterTicketRecipe fromNetwork(@Nonnull RegistryFriendlyByteBuf buffer) {
        return new MasterTicketRecipe(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer), ResourceLocation.STREAM_CODEC.decode(buffer));
    }
    private static void toNetwork(@Nonnull RegistryFriendlyByteBuf buffer, @Nonnull MasterTicketRecipe recipe) {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.ingredient);
        ResourceLocation.STREAM_CODEC.encode(buffer,recipe.resultID());
    }

    public static class Serializer implements RecipeSerializer<MasterTicketRecipe>
    {

        @Nonnull
        @Override
        public MapCodec<MasterTicketRecipe> codec() { return CODEC; }
        @Nonnull
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MasterTicketRecipe> streamCodec() { return STREAM_CODEC; }

    }

}
