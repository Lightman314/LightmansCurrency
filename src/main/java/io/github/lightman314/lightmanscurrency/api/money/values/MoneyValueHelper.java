package io.github.lightman314.lightmanscurrency.api.money.values;

import io.github.lightman314.lightmanscurrency.api.LCCapabilities;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.keys.DualKey;
import io.github.lightman314.lightmanscurrency.api.helpers.resource.access.SidedItemAccess;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.player.PlayerMoneyResourceHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MoneyValueHelper {

    public static final MoneyValueHelper DEFAULT = new MoneyValueHelper();

    public static MoneyValue quickSumValues(List<MoneyValue> values)
    {
        if(values.isEmpty())
            return MoneyValue.empty();
        MoneyValue firstValue = getFirstNonEmpty(values);
        return firstValue.getTypedHelper().sumValues(values);
    }

    public static boolean isAllowedInMoneySlot(ItemStack stack,boolean allowCapabilities) { return LCRegistries.Money.VALUE_HELPER.stream().anyMatch(helper -> helper.allowInMoneySlot(stack)) || (allowCapabilities && hasMoneyCapability(stack)); }

    public static boolean hasMoneyCapability(ItemStack stack) {
        SidedItemAccess access = SidedItemAccess.forStack(stack,ISidedContext.LOGICAL_CLIENT);
        return access.getSidedCapability(LCCapabilities.Money.ITEM) != null;
    }

    /**
     * From a list of values that are presumed to have the same {@link DualKey}, returns the sum of all values within the list.
     */
    public MoneyValue sumValues(List<MoneyValue> values)
    {
        //If the list is empty, return empty
        if(values.isEmpty())
            return MoneyValue.empty();
        //If the list only has one entry, return that entry
        if(values.size() == 1)
            return values.getFirst();
        //Otherwise return the value with the sum of each entry's internal value
        MoneyValue firstValue = getFirstNonEmpty(values);
        long totalValue = 0;
        for(MoneyValue entry : values)
        {
            if(firstValue.compatibleTypes(entry))
                totalValue += entry.getInternalValue();
        }
        return firstValue.fromInternalValue(totalValue);
    }

    public List<List<MoneyValue>> groupLikeValues(List<MoneyValue> values) {
        List<MoneyValue> thisType = new ArrayList<>();
        for(MoneyValue value : values) {
            if(value.getTypedHelper() == this)
                thisType.add(value);
        }
        return this.groupLikeValuesInternal(thisType);
    }

    protected List<List<MoneyValue>> groupLikeValuesInternal(List<MoneyValue> values) {
        //By default, one value per sub-list
        return values.stream().map(List::of).toList();
    }

    protected final List<List<MoneyValue>> groupLikeValues(List<MoneyValue> values,int countPerGroup) {
        List<List<MoneyValue>> result = new ArrayList<>();
        List<MoneyValue> temp = new ArrayList<>();
        for(MoneyValue val : values) {
            temp.add(val);
            if(temp.size() >= countPerGroup) {
                result.add(temp);
                temp = new ArrayList<>();
            }
        }
        if(!temp.isEmpty())
            result.add(temp);
        return result;
    }

    /**
     * From a list of values that could have any {@link DualKey}, append text components to the
     * tooltip via the {@code builder}.<br>
     * Intended to allow some money value types to place multiple Money Value entries into a single line,
     * but the default implementation simply adds each value to its own line.<br>
     * Custom implementations should ignore Money Values that are not their specific type
     * @see io.github.lightman314.lightmanscurrency.api.money.MoneyDisplayHelper#contentsAsMultiLineText(MoneyResourceHandler, ChatFormatting...) MoneyDisplayHelper#contentsAsText(MoneyResourceHandler, ChatFormatting...)
     */
    public final void appendTextTooltips(Consumer<Component> builder,List<MoneyValue> values)
    {
        List<MoneyValue> thisType = new ArrayList<>();
        for(MoneyValue value : values)
        {
            //Only add to the tooltip if the money value uses this exact helper
            if(value.getTypedHelper() == this)
                thisType.add(value);
        }
        if(!thisType.isEmpty())
            this.appendTextTooltipsInternal(builder,thisType);
    }

    /**
     * Implentation of {@link #appendTextTooltips(Consumer, List)} that has been provided a pre-filtered list of money values that should be utilizing this exact helper
     * @param builder The tooltip builder that can be used to add a new line to the tooltip
     * @param values A list of pre-filtered money values that utilize this helper
     */
    protected void appendTextTooltipsInternal(Consumer<Component> builder,List<MoneyValue> values) {
        for(MoneyValue value : values)
            builder.accept(value.getText());
    }

    protected static MoneyValue getFirstNonEmpty(List<MoneyValue> values)
    {
        for(MoneyValue value : values)
        {
            if(!value.isEmpty())
                return value;
        }
        return MoneyValue.empty();
    }

    /**
     * Creates a {@link PlayerMoneyResourceHandler} for the player which can be used to give or take money from the player directly.
     * @param player The player to create the Resource Handler for.
     * @param allowOverflow Whether the players inventory can be used to handle overflowing items (i.e. coins that won't fit in their wallet).
     * @return A Player Money Resource Handler that can accurately insert or extract money from the player.
     */
    @Nullable
    public PlayerMoneyResourceHandler createMoneyHandlerForPlayer(Player player,boolean allowOverflow) { return null; }

    /**
     * Creates a {@link MoneyResourceHandler}
     * @implSpec Ensure that the {@code overflowHandler} is only utilized by the generated Money Resource Handler <b>after</b> the transaction has been commited
     */
    @Nullable
    public MoneyResourceHandler wrapContainer(ResourceHandler<ItemResource> itemResource,BiConsumer<ItemStack, TransactionContext> overflowHandler, ISidedContext context) { return null; }

    public boolean allowInMoneySlot(ItemStack stack) { return false; }

}