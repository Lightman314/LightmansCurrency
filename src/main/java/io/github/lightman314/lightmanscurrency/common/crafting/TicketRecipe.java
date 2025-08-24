package io.github.lightman314.lightmanscurrency.common.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.core.ModRecipes;
import io.github.lightman314.lightmanscurrency.common.crafting.durability.DurabilityData;
import io.github.lightman314.lightmanscurrency.common.crafting.input.TicketStationRecipeInput;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TicketRecipe implements TicketStationRecipe {

    public static final MapCodec<TicketRecipe> CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                    Ingredient.CODEC_NONEMPTY.fieldOf("masterTicket").forGetter(r -> r.masterIngredient),
                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(r -> r.ingredient),
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("result").forGetter(r -> r.ticketResult),
                    DurabilityData.CODEC.optionalFieldOf("durability").forGetter(r -> r.durability.asOptional())
            ).apply(builder,TicketRecipe::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf,TicketRecipe> STREAM_CODEC = StreamCodec.of(TicketRecipe::toNetwork,TicketRecipe::fromNetwork);

    private final Ingredient masterIngredient;
    public Ingredient getMasterIngredient() { return this.masterIngredient; }
    private final Ingredient ingredient;

    private final Item ticketResult;

    private final DurabilityData durability;

    private TicketRecipe(Ingredient masterIngredient, Ingredient ingredient, Item result, Optional<DurabilityData> durabilityData) { this(masterIngredient,ingredient,result,durabilityData.orElse(DurabilityData.NULL)); }
    public TicketRecipe(Ingredient masterIngredient, Ingredient ingredient, Item result, DurabilityData durabilityData)
    {
        this.masterIngredient = masterIngredient;
        this.ingredient = ingredient;
        this.ticketResult = result;
        this.durability = durabilityData;
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
    public ItemStack assemble(TicketStationRecipeInput container, HolderLookup.Provider lookup) { return this.applyDurability(TicketItem.CraftTicket(container.getItem(0), this.ticketResult),container.data); }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return true; }
    
    @Override
    public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
        if(this.masterIngredient.getItems().length == 0)
            return ItemStack.EMPTY;
        return TicketItem.CraftTicket(TicketItem.CreateTicket(this.masterIngredient.getItems()[0].getItem(), -1), this.ticketResult);
    }

    @Override
    public ItemStack peekAtResult(Container container, ExtraData data) { return this.applyDurability(TicketItem.CraftTicket(container.getItem(0), this.ticketResult),data); }

    @Override
    public ItemStack assembleWithKiosk(ItemStack sellItem, ExtraData data) { return this.applyDurability(TicketItem.CraftTicket(sellItem,this.ticketResult),data); }

    private ItemStack applyDurability(ItemStack ticket,ExtraData data)
    {
        if(this.durability.isValid() && data.durability() > 0 && this.durability.test(data.durability()))
            ticket.set(ModDataComponents.TICKET_USES,data.durability());
        return ticket;
    }

    @Override
    public RecipeSerializer<?> getSerializer() { return ModRecipes.TICKET.get(); }

    private static TicketRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        return new TicketRecipe(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer), Ingredient.CONTENTS_STREAM_CODEC.decode(buffer), TicketStationRecipe.itemStreamCodec().decode(buffer),DurabilityData.STREAM_CODEC.decode(buffer));
    }

    private static void toNetwork(RegistryFriendlyByteBuf buffer, TicketRecipe recipe) {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer,recipe.masterIngredient);
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer,recipe.ingredient);
        TicketStationRecipe.itemStreamCodec().encode(buffer,recipe.ticketResult);
        DurabilityData.STREAM_CODEC.encode(buffer,recipe.durability);
    }

    public static class Serializer implements RecipeSerializer<TicketRecipe>
    {
        @Override
        public MapCodec<TicketRecipe> codec() { return CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TicketRecipe> streamCodec() { return STREAM_CODEC; }
    }

}
