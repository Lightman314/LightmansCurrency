package io.github.lightman314.lightmanscurrency.features.tickets.recipe;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.RecipeHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.core.LCRecipeBookCategories;
import io.github.lightman314.lightmanscurrency.core.LCRecipeTypes;
import io.github.lightman314.lightmanscurrency.features.trader.item.requirements.IngredientRequirement;
import io.github.lightman314.lightmanscurrency.features.trader.item.requirements.ItemRequirement;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class TicketStationRecipe implements Recipe<TicketStationRecipeInput> {

    public static final TextEntry JEI_INFO_DURABILITY = TextEntry.jeiInfo(LCApi.MODID,"ticket_station.durability");
    public static final TextEntry JEI_INFO_DURABILITY_INFINITE = TextEntry.tooltip(LCApi.MODID,"ticket_station.durability.infinite");

    private final String group;

    public TicketStationRecipe(String group) { this.group = group; }

    @Override
    public final String group() { return this.group; }

    @Override
    public final RecipeBookCategory recipeBookCategory() { return LCRecipeBookCategories.TICKET_STATION.get(); }

    @Override
    public final boolean showNotification() { return true; }

    @Override
    public final RecipeType<? extends Recipe<TicketStationRecipeInput>> getType() { return LCRecipeTypes.TICKET_STATION.get(); }

    public abstract boolean consumeModifier();
    public abstract boolean validModifier(ItemStack stack);

    public abstract SlotDisplay jeiModifierList();
    public abstract boolean validIngredient(ItemStack stack);

    public abstract Ingredient getIngredient();

    public abstract List<ItemStack> jeiResultList();

    public final boolean validData(TicketStationRecipeInput input) { return this.validData(input.getData()); }
    public boolean validData(TicketRecipeData data) { return this.validCode(data.code()); }

    public boolean requiredCodeInput() { return false; }
    public boolean validCode(String code) { return !this.requiredCodeInput() || TicketStationRecipeInput.isValidCode(code); }

    public DurabilityData getDurabilityData() { return DurabilityData.NULL; }
    public boolean requiredDurabilityInput() { return this.getDurabilityData().requiresInput(); }
    public int validateDurability(int value,boolean roundUp) {
        DurabilityData data = this.getDurabilityData();
        if(!data.isValid())
            return value;
        if(value > data.max)
            value = data.max;
        if(value < data.min) {
            if(data.allowInfinite && !roundUp)
                value = 0;
            else
                value = data.min;
        }
        return value;
    }

    /**
     * Don't put "valid code" check here, as it will prevent the recipe from being visible
     * as the code input won't appear until after a recipe is selected that requires it
     */
    @Override
    public boolean matches(TicketStationRecipeInput input,Level level) {
        return this.validModifier(input.getItem(0)) && this.validIngredient(input.getItem(1));
    }

    public void afterAssembly(TicketStationRecipeInput input) {}

    //Ticket Kiosk Crafting
    public boolean matchesTicketKioskSellItem(ItemStack sellItem) { return !this.consumeModifier() && this.validModifier(sellItem); }
    public boolean allowIgnoreKioskRecipe() { return false; }
    public abstract ItemStack assembledWithKiosk(ItemStack sellItem,ISidedContext context,TicketRecipeData data);
    public ItemRequirement getKioskStorageRequirement(ItemStack sellItem) { return new IngredientRequirement(this.getIngredient(),sellItem.getCount()); }

    protected static SlotDisplay exampleDyeList() {
        List<ItemStack> list = new ArrayList<>();
        for(DyeColor color : DyeColor.values()) {
            for(Holder<Item> d : BuiltInRegistries.ITEM.getTagOrEmpty(color.getTag())) {
                list.add(new ItemStack(d));
                break;
            }
        }
        return RecipeHelper.asSlotDisplay(list);
    }

    @SuppressWarnings("deprecation")
    protected static List<ItemStack> exampleDyedItemList(Ingredient ingredient) {
        List<ItemStack> list = new ArrayList<>();
        ingredient.items().forEach(item -> list.addAll(exampleDyedItemList(() -> new ItemStack(item))));
        return list;
    }
    protected static List<ItemStack> exampleDyedItemList(ItemStackTemplate item) { return exampleDyedItemList(item::create); }
    protected static List<ItemStack> exampleDyedItemList(Supplier<ItemStack> builder) {
        List<ItemStack> result = new ArrayList<>();
        for(DyeColor color : DyeColor.values()) {
            ItemStack stack = builder.get();
            stack.set(DataComponents.DYED_COLOR,new DyedItemColor(color.getTextureDiffuseColor()));
            result.add(stack);
        }
        return result;
    }

}