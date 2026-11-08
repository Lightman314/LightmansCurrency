package io.github.lightman314.lightmanscurrency.features.tickets.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.RecipeHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.List;
import java.util.Optional;

public class TicketRecipe extends TicketStationRecipe {

    private static final MapCodec<TicketRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.STRING.optionalFieldOf("group","").forGetter(TicketRecipe::group),
            Ingredient.CODEC.fieldOf("masterTicket").forGetter(TicketRecipe::getMasterIngredient),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(TicketRecipe::getIngredient),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result),
            DurabilityData.VALID_CODEC.fieldOf("durability").forGetter(r -> r.durability)
    ).apply(builder,TicketRecipe::new));
    private static final StreamCodec<RegistryFriendlyByteBuf,TicketRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,TicketRecipe::group,
            Ingredient.CONTENTS_STREAM_CODEC,TicketRecipe::getMasterIngredient,
            Ingredient.CONTENTS_STREAM_CODEC,TicketRecipe::getIngredient,
            ItemStackTemplate.STREAM_CODEC,r -> r.result,
            DurabilityData.STREAM_CODEC,r -> r.durability,
            TicketRecipe::new);

    public static final RecipeSerializer<TicketRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC,STREAM_CODEC);

    private final Ingredient masterIngredient;
    public Ingredient getMasterIngredient() { return this.masterIngredient; }
    private final Ingredient ingredient;

    private final ItemStackTemplate result;
    private final DurabilityData durability;

    private TicketRecipe(String group,Ingredient masterIngredient,Ingredient ingredient,ItemStackTemplate result,Optional<DurabilityData> durability) { this(group,masterIngredient,ingredient,result,durability.orElse(DurabilityData.NULL)); }
    public TicketRecipe(String group,Ingredient masterIngredient,Ingredient ingredient,ItemStackTemplate result,DurabilityData durability) {
        super(group);
        this.masterIngredient = masterIngredient;
        this.ingredient = ingredient;
        this.result = result;
        this.durability = durability.assertValid();
    }

    @Override
    public boolean consumeModifier() { return false; }

    @Override
    public boolean validModifier(ItemStack stack) { return this.masterIngredient.test(stack); }

    @Override
    public SlotDisplay jeiModifierList() { return RecipeHelper.asSlotDisplay(TicketStationRecipe.exampleDyedItemList(this.masterIngredient)); }

    @Override
    public boolean validIngredient(ItemStack stack) { return this.ingredient.test(stack); }

    @Override
    public Ingredient getIngredient() { return this.ingredient; }

    @Override
    public List<ItemStack> jeiResultList() { return TicketStationRecipe.exampleDyedItemList(this.result); }

    @Override
    public ItemStack assemble(TicketStationRecipeInput input) {
        ItemStack masterTicket = input.getItem(0);
        if(masterTicket.isEmpty())
            return ItemStack.EMPTY;
        ItemStack result = this.result.create();
        this.copyMasterTicketData(result,masterTicket,input.getData());
        return result;
    }

    @Override
    public ItemStack assembledWithKiosk(ItemStack sellItem,ISidedContext context,TicketRecipeData data) {
        if(sellItem.isEmpty())
            return ItemStack.EMPTY;
        ItemStack result = this.result.create();
        this.copyMasterTicketData(result,sellItem,data);
        return result;
    }

    private void copyMasterTicketData(ItemStack ticket,ItemStack masterTicket,TicketRecipeData data) {
        //Only copy the ticket id & dyed color components
        ItemHelper.copyComponent(LCDataComponents.TICKET_ID,masterTicket,ticket);
        ItemHelper.copyComponent(DataComponents.DYED_COLOR,masterTicket,ticket);
        //Apply the durability
        ticket.set(LCDataComponents.TICKET_USES,this.durability.assemble(data.durability()));
    }

    @Override
    public DurabilityData getDurabilityData() { return this.durability; }

    @Override
    public RecipeSerializer<? extends Recipe<TicketStationRecipeInput>> getSerializer() { return SERIALIZER; }

    @Override
    public PlacementInfo placementInfo() { return PlacementInfo.create(List.of(this.masterIngredient,this.ingredient)); }

}