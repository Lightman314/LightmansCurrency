package io.github.lightman314.lightmanscurrency.features.tickets.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.types.VanillaColor;
import io.github.lightman314.lightmanscurrency.features.tickets.data.TicketID;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import io.github.lightman314.lightmanscurrency.features.tickets.TicketDataCache;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.List;
import java.util.Optional;

public class MasterTicketRecipe extends TicketStationRecipe {

    private static final MapCodec<MasterTicketRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.STRING.optionalFieldOf("group","").forGetter(MasterTicketRecipe::group),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(MasterTicketRecipe::getIngredient),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result)
    ).apply(builder,MasterTicketRecipe::new));
    private static final StreamCodec<RegistryFriendlyByteBuf,MasterTicketRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,MasterTicketRecipe::group,
            Ingredient.CONTENTS_STREAM_CODEC,MasterTicketRecipe::getIngredient,
            ItemStackTemplate.STREAM_CODEC,r -> r.result,
            MasterTicketRecipe::new);

    public static final RecipeSerializer<MasterTicketRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC,STREAM_CODEC);

    private final Ingredient ingredient;
    private final ItemStackTemplate result;
    public MasterTicketRecipe(String group,Ingredient ingredient,ItemStackTemplate result) {
        super(group);
        this.ingredient = ingredient;
        this.result = result;
    }

    @Override
    public RecipeSerializer<? extends Recipe<TicketStationRecipeInput>> getSerializer() { return SERIALIZER; }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.createFromOptionals(List.of(Optional.empty(),Optional.of(this.ingredient)));
    }

    @Override
    public boolean consumeModifier() { return true; }

    @Override
    public boolean validModifier(ItemStack stack) { return stack.isEmpty() || stack.has(DataComponents.DYE); }

    @Override
    public SlotDisplay jeiModifierList() { return TicketStationRecipe.exampleDyeList(); }

    @Override
    public boolean validIngredient(ItemStack stack) { return this.ingredient.test(stack); }

    @Override
    public Ingredient getIngredient() { return this.ingredient; }

    @Override
    public List<ItemStack> jeiResultList() { return TicketStationRecipe.exampleDyedItemList(this.result); }

    @Override
    public ItemStack assemble(TicketStationRecipeInput input) {
        long ticketID  = TicketDataCache.TYPE.get(input).peekNextID();
        ItemStack modifier = input.getItem(0);
        DyeColor color;
        if(modifier.has(DataComponents.DYE))
            color = modifier.get(DataComponents.DYE);
        else
            color = EnumHelper.enumFromOrdinal((int)(ticketID % VanillaColor.values().length),VanillaColor.values(),VanillaColor.WHITE).color;
        ItemStack result = this.result.create();
        result.set(LCDataComponents.TICKET_ID,new TicketID(ticketID));
        result.set(DataComponents.DYED_COLOR,new DyedItemColor(color.getTextureDiffuseColor()));
        return result;
    }

    @Override
    public void afterAssembly(TicketStationRecipeInput input) {
        TicketDataCache.TYPE.get(input).useNextID();
    }

    @Override
    public boolean matchesTicketKioskSellItem(ItemStack sellItem) { return false; }
    @Override
    public ItemStack assembledWithKiosk(ItemStack sellItem,ISidedContext context,TicketRecipeData data) { return ItemStack.EMPTY; }

}