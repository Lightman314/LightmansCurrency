package io.github.lightman314.lightmanscurrency.api.money.values;

import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
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

    /**
     * From a list of values that are presumed to have the same {@link MoneyKey}, returns the sum of all values within the list.
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

    /**
     * From a list of values that could have any {@link MoneyKey}, append text components to the
     * tooltip via the {@code builder}.<br>
     * Intended to allow some money value types to place multiple Money Value entries into a single line,
     * but the default implementation simply adds each value to its own line.<br>
     * Custom implementations should ignore Money Values that are not their specific type
     * @see io.github.lightman314.lightmanscurrency.api.money.MoneyDisplayHelper#contentsAsMultiLineText(MoneyResourceHandler, ChatFormatting...) MoneyDisplayHelper#contentsAsText(MoneyResourceHandler, ChatFormatting...)
     */
    public void appendTextTooltips(Consumer<Component> builder,List<MoneyValue> values)
    {
        for(MoneyValue value : values)
        {
            //Only add to the tooltip if the money value uses this exact helper
            if(value.getTypedHelper() == this)
                builder.accept(value.getText());
        }
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

}