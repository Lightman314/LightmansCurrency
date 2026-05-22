package io.github.lightman314.lightmanscurrency.api.money.capability;

import io.github.lightman314.lightmanscurrency.api.money.capability.implementations.MultiMoneyHolder;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.builtin.EmptyMoneyHolder;
import net.minecraft.network.chat.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Extension of {@link IMoneyHandler} with additional methods for sorting and displaying the source of the money<br>
 * Primarily used in {@link io.github.lightman314.lightmanscurrency.api.traders.trade.TradeContext TradeContext} to allow multiple sources of money (for example, both the players wallet <b>and</b> coins/items in the Money Slots)
 */
public interface IMoneyHolder extends IMoneyHandler {

    IMoneyHolder EMPTY = new EmptyMoneyHolder();

    /**
     * Sorting priority.<br>
     * Higher values are given to last<br>
     * If {@link #inversePriority()} is not overridden, lower values are taken from last
     */
    default int priority() { return 0; }

    /**
     * Inverted value of the Sorting Priority.<br>
     * Defaults to {@link #priority()} * -1<br>
     * Higher values are taken from last
     */
    default int inversePriority() { return this.priority() * -1; }

    /**
     * Adds text to the tooltip detailing the items of this money holder.<br>
     * Typically formatted as:<code><br>Title<br>Contents 1<br>Contents 2<br>etc.</code><br>
     */
    default void formatTooltip(List<Component> tooltip)
    {
        defaultTooltipFormat(tooltip,this.getTooltipTitle(),this.getStoredMoney());
    }
    static void defaultTooltipFormat(List<Component> tooltip, Component title, MoneyView contents)
    {
        if(contents.isEmpty())
            return;
        tooltip.add(title);
        tooltip.addAll(contents.getAllText());
    }
    Component getTooltipTitle();
    static void sortPayFirst(List<IMoneyHolder> list) { list.sort(Comparator.comparingInt(IMoneyHolder::priority)); }
    static void sortTakeFirst(List<IMoneyHolder> list) { list.sort(Comparator.comparingInt(IMoneyHolder::inversePriority)); }

    static IMoneyHolder combine(List<IMoneyHolder> list) { return new MultiMoneyHolder(list); }

}
