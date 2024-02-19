package io.github.lightman314.lightmanscurrency.api.money.value.holder;

import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;

public interface IMoneyHolder extends IMoneyHandler {

    /**
     * Sorting priority.<br>
     * Higher values are given to last, but taken from first.
     */
    default int priority() { return 0; }

    /**
     * Inverted value of the Sorting Priority.<br>
     * Defaults to {@link #priority()} * -1
     */
    default int inversePriority() { return this.priority() * -1; }

    /**
     * Adds text to the tooltip detailing the contents of this money holder.<br>
     * Typically formatted as:<code><br>Title<br>Contents 1<br>Contents 2<br>etc.</code><br>
     */
    default void formatTooltip(@Nonnull List<Component> tooltip)
    {
        defaultTooltipFormat(tooltip, this.getTooltipTitle(), this.getStoredMoney());
    }
    static void defaultTooltipFormat(@Nonnull List<Component> tooltip, @Nonnull Component title, @Nonnull MoneyView contents)
    {
        if(contents.isEmpty())
            return;
        tooltip.add(title);
        for(MoneyValue val : contents.allValues())
            tooltip.add(val.getText());
    }
    Component getTooltipTitle();
    static void sortPayFirst(@Nonnull List<IMoneyHolder> list) { list.sort(Comparator.comparingInt(IMoneyHolder::priority)); }
    static void sortTakeFirst(@Nonnull List<IMoneyHolder> list) { list.sort(Comparator.comparingInt(IMoneyHolder::inversePriority)); }

}
