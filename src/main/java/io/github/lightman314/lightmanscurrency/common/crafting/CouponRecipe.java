package io.github.lightman314.lightmanscurrency.common.crafting;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.common.core.ModRecipes;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.crafting.durability.DurabilityData;
import io.github.lightman314.lightmanscurrency.common.crafting.input.TicketStationRecipeInput;
import io.github.lightman314.lightmanscurrency.common.items.CouponItem;
import io.github.lightman314.lightmanscurrency.common.menus.slots.ticket.TicketModifierSlot;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CouponRecipe implements TicketStationRecipe {

    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final Item result;
    private final DurabilityData durability;
    private ResourceLocation resultID() { return ForgeRegistries.ITEMS.getKey(this.result); }

    public CouponRecipe(ResourceLocation id, Ingredient ingredient, Item result, Optional<DurabilityData> durabilityData) { this(id,ingredient,result,durabilityData.orElse(new DurabilityData(true,0,99))); }
    public CouponRecipe(ResourceLocation id, Ingredient ingredient, Item result, DurabilityData durability)
    {
        this.id = id;
        this.ingredient = ingredient;
        this.result = result;
        this.durability = durability;
    }

    @Override
    public ResourceLocation getId() { return this.id; }
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
    public boolean validModifier(ItemStack stack) { return stack.isEmpty() || stack.is(Tags.Items.DYES); }
    @Override
    public boolean validIngredient(ItemStack stack) { return this.ingredient.test(stack); }

    @Override
    public ItemStack assemble(TicketStationRecipeInput container, RegistryAccess lookup) {
        ItemStack dyeStack = container.getItem(0);
        Color dyeColor = TicketModifierSlot.getColorFromDye(dyeStack);
        int color = dyeColor == null ? 0xFFFFFF : dyeColor.hexColor;
        return CouponItem.CreateCoupon(this.result,container.data.code(),container.data.durability(),color);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return true; }

    @Override
    public ItemStack getResultItem(RegistryAccess lookup) { return CouponItem.CreateCoupon(this.result,"",0); }
    
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

    public static class Serializer implements RecipeSerializer<CouponRecipe>
    {

        @Override
        public CouponRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient ingredient = CraftingHelper.getIngredient(json.get("ingredient"),false);
            Item result = CraftingHelper.getItem(GsonHelper.getAsString(json,"result"),false);
            Optional<DurabilityData> durability = DurabilityData.parseValid(json,"durability");
            return new CouponRecipe(id,ingredient,result,durability);
        }

        @Override
        public @Nullable CouponRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            Item result = CraftingHelper.getItem(buffer.readUtf(),false);
            DurabilityData durability = DurabilityData.decode(buffer);
            return new CouponRecipe(id,ingredient,result,durability);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, CouponRecipe recipe) {
            recipe.ingredient.toNetwork(buffer);
            buffer.writeUtf(recipe.resultID().toString());
            recipe.durability.encode(buffer);
        }
    }

}