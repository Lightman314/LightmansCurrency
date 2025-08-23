package io.github.lightman314.lightmanscurrency.common.crafting;

import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.crafting.durability.DurabilityData;
import io.github.lightman314.lightmanscurrency.common.crafting.input.TicketStationRecipeInput;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.util.ItemRequirement;
import io.netty.buffer.ByteBuf;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface TicketStationRecipe extends Recipe<TicketStationRecipeInput> {

    Predicate<String> CODE_PREDICATE = s -> {
        if(s.isBlank())
            return false;
        if(s.length() > 16)
            return false;
        for(int i = 0; i < s.length(); ++i)
        {
            if(!validCodeChar(s.charAt(i)))
                return false;
        }
        return true;
    };

    Predicate<String> CODE_INPUT_PREDICATE = s -> s.isEmpty() || CODE_PREDICATE.test(s);

    static boolean validCodeChar(char codeChar) { return codeChar >= 'a' && codeChar <= 'z' || codeChar >= 'A' && codeChar <= 'Z' || codeChar >= '0' && codeChar <= '9'; }

    @Override
    default RecipeType<TicketStationRecipe> getType() { return RecipeTypes.TICKET.get(); }

    @Override
    default ItemStack getToastSymbol() { return new ItemStack(ModBlocks.TICKET_STATION.get()); }

    
    static List<ItemStack> exampleModifierList(TagKey<Item> tag, Item... extra)
    {
        List<ItemStack> result = new ArrayList<>();
        for(Item extraItem : extra)
            result.add(new ItemStack(extraItem));
        for(Holder<Item> item : BuiltInRegistries.ITEM.getTagOrEmpty(tag))
            result.add(new ItemStack(item.value()));
        return result;
    }
    
    static List<ItemStack> exampleTicketList(Supplier<? extends ItemLike> item) { return exampleTicketList(item.get().asItem()); }
    
    static List<ItemStack> exampleTicketList(Ingredient ingredient)
    {
        List<ItemStack> result = new ArrayList<>();
        for(ItemStack item : ingredient.getItems())
            result.addAll(exampleTicketList(item.getItem()));
        return result;
    }
    static List<ItemStack> exampleTicketList(Item item)
    {
        List<ItemStack> result = new ArrayList<>();
        for(Color color : Color.values())
            result.add(TicketItem.CreateExampleTicket(item, color));
        return result;
    }

    boolean consumeModifier();

    boolean validModifier(ItemStack stack);
    
    List<ItemStack> jeiModifierList();
    boolean validIngredient(ItemStack stack);
    
    Ingredient getIngredient();

    
    ItemStack peekAtResult(Container container, ExtraData data);
    
    ItemStack exampleResult();

    default boolean validData(ExtraData data) { return this.validCode(data.code) && this.validDurability(data.durability); }

    default boolean requiredCodeInput() { return false; }
    default boolean validCode(String code) { return !this.requiredCodeInput() || CODE_PREDICATE.test(code); }

    default boolean requiredDurabilityInput() { return this.getDurabilityData().isValid(); }
    default boolean validDurability(int durability) { return this.getDurabilityData().test(durability); }
    default DurabilityData getDurabilityData() { return DurabilityData.NULL; }
    default int validateDurability(int value, boolean roundUp)
    {
        DurabilityData data = this.getDurabilityData();
        if(!data.isValid())
            return value;
        if(value > data.max)
            value = data.max;
        if(value < data.min)
        {
            if(data.allowInfinite && !roundUp)
                value = 0;
            else
                value = data.min;
        }
        return value;
    }

    //Don't put "valid code" check here, as it will prevent the recipe from being visible as the code input won't appear unless they can first select the code requiring recipe
    @Override
    default boolean matches(TicketStationRecipeInput container, Level level) { return this.validModifier(container.getItem(0)) && this.validIngredient(container.getItem(1)); }

    //Ticket Kiosk Crafting
    default boolean matchesTicketKioskSellItem(ItemStack sellItem) { return !this.consumeModifier() && this.validModifier(sellItem); }
    default boolean allowIgnoreKioskRecipe() { return false; }
    ItemStack assembleWithKiosk(ItemStack sellItem,ExtraData data);
    default ItemRequirement getKioskStorageRequirement(ItemStack sellItem) { return ItemRequirement.of(this.getIngredient(),sellItem.getCount()); }

    record ExtraData(String code, int durability) { public static final ExtraData EMPTY = new ExtraData("",0); }

    static StreamCodec<ByteBuf,Item> itemStreamCodec() { return ResourceLocation.STREAM_CODEC.map(BuiltInRegistries.ITEM::get, BuiltInRegistries.ITEM::getKey); }

}
