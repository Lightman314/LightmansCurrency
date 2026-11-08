package io.github.lightman314.lightmanscurrency.features.tickets.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.features.tickets.TicketHelper;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import io.github.lightman314.lightmanscurrency.features.tickets.data.EncryptedCodeEntry;
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

public class CouponRecipe extends TicketStationRecipe {

    private static final MapCodec<CouponRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.STRING.optionalFieldOf("group","").forGetter(CouponRecipe::group),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(CouponRecipe::getIngredient),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result),
            DurabilityData.VALID_CODEC.fieldOf("durability").forGetter(CouponRecipe::getDurabilityData)
    ).apply(builder,CouponRecipe::new));
    private static final StreamCodec<RegistryFriendlyByteBuf, CouponRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CouponRecipe::group,
            Ingredient.CONTENTS_STREAM_CODEC, CouponRecipe::getIngredient,
            ItemStackTemplate.STREAM_CODEC,r -> r.result,
            DurabilityData.STREAM_CODEC,CouponRecipe::getDurabilityData,
            CouponRecipe::new);

    public static final RecipeSerializer<CouponRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC,STREAM_CODEC);

    private final Ingredient ingredient;
    private final ItemStackTemplate result;
    private final DurabilityData durability;
    public CouponRecipe(String group,Ingredient ingredient,ItemStackTemplate result,DurabilityData durability) {
        super(group);
        this.ingredient = ingredient;
        this.result = result;
        this.durability = durability.assertValid();
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
        ItemStack modifier = input.getItem(0);
        DyeColor color;
        if(modifier.has(DataComponents.DYE))
            color = modifier.get(DataComponents.DYE);
        else
            color = DyeColor.WHITE;
        ItemStack result = this.result.create();
        this.assembleCoupon(result,color,input,input.getData());
        return result;
    }

    @Override
    public ItemStack assembledWithKiosk(ItemStack sellItem,ISidedContext context,TicketRecipeData data) {
        ItemStack result = this.result.create();
        this.assembleCoupon(result,DyeColor.WHITE,context,data);
        return result;
    }

    private void assembleCoupon(ItemStack result,DyeColor color,ISidedContext context,TicketRecipeData data) {
        //Set the coupon code
        result.set(LCDataComponents.COUPON_CODE,new EncryptedCodeEntry(data.code(),context));
        //Set the color
        result.set(DataComponents.DYED_COLOR,new DyedItemColor(color.getTextureDiffuseColor()));
        //Set the durability
        result.set(LCDataComponents.TICKET_USES,this.durability.assemble(data.durability()));
    }

    @Override
    public boolean matchesTicketKioskSellItem(ItemStack sellItem) { return this.validIngredient(sellItem) && !TicketHelper.isMasterTicket(sellItem); }

    @Override
    public boolean requiredCodeInput() { return true; }

    @Override
    public DurabilityData getDurabilityData() { return this.durability; }

}