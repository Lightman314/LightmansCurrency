package io.github.lightman314.lightmanscurrency.common.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.common.core.ModRecipeSerializers;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.crafting.input.TicketStationRecipeInput;
import io.github.lightman314.lightmanscurrency.common.data.types.TicketDataCache;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.menus.slots.ticket.TicketModifierSlot;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.Tags;

import java.util.List;

public class MasterTicketRecipe implements TicketStationRecipe {

    public static final MapCodec<MasterTicketRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(r -> r.ingredient),
                            BuiltInRegistries.ITEM.byNameCodec().fieldOf("result").forGetter(r -> r.result))
                    .apply(builder, MasterTicketRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf,MasterTicketRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,MasterTicketRecipe::getIngredient,
            ByteBufCodecs.registry(Registries.ITEM),r -> r.result,
            MasterTicketRecipe::new);

            //StreamCodec.of(MasterTicketRecipe::toNetwork,MasterTicketRecipe::fromNetwork);

    private final Ingredient ingredient;
    private final Item result;

    //Constructor for codec
    public MasterTicketRecipe(Ingredient ingredient, Item result)
    {
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
    public ItemStack assemble(TicketStationRecipeInput container, HolderLookup.Provider lookup) {
        long nextTicketID = TicketDataCache.TYPE.isLoaded(false) ? TicketDataCache.TYPE.get(false).createNextID() : -100L;
        ItemStack dyeStack = container.getItem(0);
        Color dyeColor = TicketModifierSlot.getColorFromDye(dyeStack);
        int color = dyeColor == null ? TicketItem.GetDefaultTicketColor(nextTicketID) : dyeColor.hexColor;
        return TicketItem.CreateTicket(this.result, nextTicketID, color, 1);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return true; }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider lookup) {
        long nextTicketID = TicketDataCache.TYPE.getUnknown().peekNextID();
        int color = TicketItem.GetDefaultTicketColor(nextTicketID);
        return TicketItem.CreateTicket(this.result, nextTicketID, color, 1);
    }

    @Override
    public ItemStack peekAtResult(TicketStationRecipeInput input) {
        long nextTicketID = TicketDataCache.TYPE.getUnknown().peekNextID();
        ItemStack dyeStack = input.getItem(0);
        Color dyeColor = TicketModifierSlot.getColorFromDye(dyeStack);
        if(dyeColor != null)
            return TicketItem.CreateTicket(this.result,nextTicketID,dyeColor.hexColor);
        else
            return TicketItem.CreateTicket(this.result,nextTicketID);
    }

    @Override
    public ItemStack assembleWithKiosk(ItemStack sellItem, ExtraData data) { return ItemStack.EMPTY; }

    @Override
    public RecipeSerializer<?> getSerializer() { return ModRecipeSerializers.TICKET_MASTER.get(); }

}
