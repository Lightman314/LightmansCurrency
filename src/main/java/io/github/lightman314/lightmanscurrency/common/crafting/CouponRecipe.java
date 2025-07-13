package io.github.lightman314.lightmanscurrency.common.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.common.core.ModRecipes;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.crafting.input.TicketStationRecipeInput;
import io.github.lightman314.lightmanscurrency.common.items.CouponItem;
import io.github.lightman314.lightmanscurrency.common.menus.slots.ticket.TicketModifierSlot;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
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
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CouponRecipe implements TicketStationRecipe {

    public static final MapCodec<CouponRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(r -> r.ingredient),
                            ResourceLocation.CODEC.fieldOf("result").forGetter(CouponRecipe::resultID))
                    .apply(builder, CouponRecipe::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, CouponRecipe> STREAM_CODEC = StreamCodec.of(CouponRecipe::toNetwork, CouponRecipe::fromNetwork);

    private final Ingredient ingredient;
    private final Item result;
    private ResourceLocation resultID() { return BuiltInRegistries.ITEM.getKey(this.result); }

    //Constructor for codec
    private CouponRecipe(@Nonnull Ingredient ingredient, @Nonnull ResourceLocation resultID) { this(ingredient, BuiltInRegistries.ITEM.get(resultID)); }
    public CouponRecipe(@Nonnull Ingredient ingredient, @Nonnull Item result)
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
    public ItemStack exampleResult() { return CouponItem.CreateCoupon(this.result, ""); }
    @Override
    public boolean requiredCodeInput() { return true; }

    @Override
    public boolean validModifier(@Nonnull ItemStack stack) { return stack.isEmpty() || stack.is(Tags.Items.DYES); }
    @Override
    public boolean validIngredient(@Nonnull ItemStack stack) { return this.ingredient.test(stack); }

    @Nonnull
    @Override
    public ItemStack assemble(TicketStationRecipeInput container, @Nonnull HolderLookup.Provider lookup) {
        ItemStack dyeStack = container.getItem(0);
        Color dyeColor = TicketModifierSlot.getColorFromDye(dyeStack);
        int color = dyeColor == null ? 0xFFFFFF : dyeColor.hexColor;
        return CouponItem.CreateCoupon(this.result,container.getCode(),color);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return true; }

    @Nonnull
    @Override
    public ItemStack getResultItem(@Nonnull HolderLookup.Provider lookup) { return CouponItem.CreateCoupon(this.result,""); }

    @Nonnull
    @Override
    public ItemStack peekAtResult(@Nonnull Container container, @Nonnull String code) {
        ItemStack dyeStack = container.getItem(0);
        Color dyeColor = TicketModifierSlot.getColorFromDye(dyeStack);
        if(dyeColor != null)
            return CouponItem.CreateCoupon(this.result,code,dyeColor.hexColor);
        else
            return CouponItem.CreateCoupon(this.result,code);
    }

    @Override
    public boolean matchesTicketKioskSellItem(ItemStack sellItem) { return this.validIngredient(sellItem) && !InventoryUtil.ItemHasTag(sellItem, LCTags.Items.TICKETS_MASTER); }
    @Override
    public boolean allowIgnoreKioskRecipe() { return true; }
    @Override
    public ItemStack assembleWithKiosk(ItemStack sellItem, String code) { return CouponItem.CreateCoupon(this.result,code); }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() { return ModRecipes.COUPON.get(); }

    @Nonnull
    private static CouponRecipe fromNetwork(@Nonnull RegistryFriendlyByteBuf buffer) {
        return new CouponRecipe(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer), ResourceLocation.STREAM_CODEC.decode(buffer));
    }
    private static void toNetwork(@Nonnull RegistryFriendlyByteBuf buffer, @Nonnull CouponRecipe recipe) {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.ingredient);
        ResourceLocation.STREAM_CODEC.encode(buffer,recipe.resultID());
    }

    public static class Serializer implements RecipeSerializer<CouponRecipe>
    {

        @Nonnull
        @Override
        public MapCodec<CouponRecipe> codec() { return CODEC; }
        @Nonnull
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CouponRecipe> streamCodec() { return STREAM_CODEC; }

    }

}
