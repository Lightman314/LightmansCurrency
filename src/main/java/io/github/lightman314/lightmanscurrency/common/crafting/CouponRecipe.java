package io.github.lightman314.lightmanscurrency.common.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.common.core.ModRecipes;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.crafting.durability.DurabilityData;
import io.github.lightman314.lightmanscurrency.common.crafting.input.TicketStationRecipeInput;
import io.github.lightman314.lightmanscurrency.common.items.CouponItem;
import io.github.lightman314.lightmanscurrency.common.menus.slots.ticket.TicketModifierSlot;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CouponRecipe implements TicketStationRecipe {

    public static final MapCodec<CouponRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(r -> r.ingredient),
                            BuiltInRegistries.ITEM.byNameCodec().fieldOf("result").forGetter(r -> r.result),
                            DurabilityData.VALID_CODEC.fieldOf("durability").forGetter(r -> r.durability))
                    .apply(builder, CouponRecipe::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, CouponRecipe> STREAM_CODEC = StreamCodec.of(CouponRecipe::toNetwork, CouponRecipe::fromNetwork);

    private final Ingredient ingredient;
    private final Item result;
    private final DurabilityData durability;
    //Constructor for codec
    private CouponRecipe(Ingredient ingredient, Item result, Optional<DurabilityData> durabilityData) { this(ingredient,result,durabilityData.orElse(new DurabilityData(true,0,99))); }
    public CouponRecipe(Ingredient ingredient, Item result, DurabilityData durability)
    {
        this.ingredient = ingredient;
        this.result = result;
        this.durability = durability;
    }

    @Override
    public boolean consumeModifier() { return true; }
    
    @Override
    public List<ItemStack> jeiModifierList() { return TicketStationRecipe.exampleModifierList(Tags.Items.DYES, Items.AIR); }
    
    @Override
    public Ingredient getIngredient() { return this.ingredient; }
    
    @Override
    public ItemStack exampleResult() { return CouponItem.CreateCoupon(this.result, "",0); }
    @Override
    public boolean requiredCodeInput() { return true; }
    @Override
    public DurabilityData getDurabilityData() { return this.durability; }

    @Override
    public boolean validModifier(ItemStack stack) { return stack.isEmpty() || stack.is(Tags.Items.DYES); }
    @Override
    public boolean validIngredient(ItemStack stack) { return this.ingredient.test(stack); }

    
    @Override
    public ItemStack assemble(TicketStationRecipeInput container, HolderLookup.Provider lookup) {
        ItemStack dyeStack = container.getItem(0);
        Color dyeColor = TicketModifierSlot.getColorFromDye(dyeStack);
        int color = dyeColor == null ? 0xFFFFFF : dyeColor.hexColor;
        return CouponItem.CreateCoupon(this.result,container.data.code(),container.data.durability(),color);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return true; }

    
    @Override
    public ItemStack getResultItem(HolderLookup.Provider lookup) { return CouponItem.CreateCoupon(this.result,"",0); }

    
    @Override
    public ItemStack peekAtResult(Container container, ExtraData data) {
        ItemStack dyeStack = container.getItem(0);
        Color dyeColor = TicketModifierSlot.getColorFromDye(dyeStack);
        if(dyeColor != null)
            return CouponItem.CreateCoupon(this.result,data.code(),data.durability(),dyeColor.hexColor);
        else
            return CouponItem.CreateCoupon(this.result,data.code(),data.durability());
    }

    @Override
    public boolean matchesTicketKioskSellItem(ItemStack sellItem) { return this.validIngredient(sellItem) && !InventoryUtil.ItemHasTag(sellItem, LCTags.Items.TICKETS_MASTER); }
    @Override
    public boolean allowIgnoreKioskRecipe() { return true; }
    @Override
    public ItemStack assembleWithKiosk(ItemStack sellItem, ExtraData data) { return CouponItem.CreateCoupon(this.result,data.code(),data.durability()); }

    @Override
    public RecipeSerializer<?> getSerializer() { return ModRecipes.COUPON.get(); }

    
    private static CouponRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        return new CouponRecipe(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),TicketStationRecipe.itemStreamCodec().decode(buffer),DurabilityData.STREAM_CODEC.decode(buffer));
    }
    private static void toNetwork(RegistryFriendlyByteBuf buffer, CouponRecipe recipe) {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.ingredient);
        TicketStationRecipe.itemStreamCodec().encode(buffer,recipe.result);
        DurabilityData.STREAM_CODEC.encode(buffer,recipe.durability);
    }

    public static class Serializer implements RecipeSerializer<CouponRecipe>
    {
        @Override
        public MapCodec<CouponRecipe> codec() { return CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CouponRecipe> streamCodec() { return STREAM_CODEC; }
    }

}
