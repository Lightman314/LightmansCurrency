package io.github.lightman314.lightmanscurrency.common.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.common.core.ModRecipeSerializers;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.crafting.durability.DurabilityData;
import io.github.lightman314.lightmanscurrency.common.crafting.input.TicketStationRecipeInput;
import io.github.lightman314.lightmanscurrency.common.items.CouponItem;
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
import java.util.Optional;

public class CouponRecipe implements TicketStationRecipe {

    public static final MapCodec<CouponRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(r -> r.ingredient),
                            BuiltInRegistries.ITEM.byNameCodec().fieldOf("result").forGetter(r -> r.result),
                            DurabilityData.VALID_CODEC.fieldOf("durability").forGetter(r -> r.durability))
                    .apply(builder, CouponRecipe::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, CouponRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,r -> r.ingredient,
            ByteBufCodecs.registry(Registries.ITEM),r -> r.result,
            DurabilityData.STREAM_CODEC,r -> r.durability,
            CouponRecipe::new);

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
    public ItemStack peekAtResult(TicketStationRecipeInput input) {
        ItemStack dyeStack = input.getItem(0);
        Color dyeColor = TicketModifierSlot.getColorFromDye(dyeStack);
        if(dyeColor != null)
            return CouponItem.CreateCoupon(this.result,input.data.code(),input.data.durability(),dyeColor.hexColor);
        else
            return CouponItem.CreateCoupon(this.result,input.data.code(),input.data.durability());
    }

    @Override
    public boolean matchesTicketKioskSellItem(ItemStack sellItem) { return this.validIngredient(sellItem) && !sellItem.is(LCTags.Items.TICKETS_MASTER); }
    @Override
    public boolean allowIgnoreKioskRecipe() { return true; }
    @Override
    public ItemStack assembleWithKiosk(ItemStack sellItem, ExtraData data) { return CouponItem.CreateCoupon(this.result,data.code(),data.durability()); }

    @Override
    public RecipeSerializer<?> getSerializer() { return ModRecipeSerializers.COUPON.get(); }

}
