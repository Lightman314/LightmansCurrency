package io.github.lightman314.lightmanscurrency.api.money.value.holder;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public interface IMoneyHolder extends IMoneyViewer {

    /**
     * Sorting priority.
     * Higher values are given to last, but taken from first.
     */
    default int priority() { return 0; }

    /**
     * Inverted value of the Sorting Priority.
     * Defaults to {@link #priority()} * -1
     */
    default int inversePriority() { return this.priority() * -1; }

    /**
     * Attempt to add the given value to the money holder.
     * @return The amount of money left to be added by another handler
     */
    @Nonnull
    MoneyValue tryAddMoney(@Nonnull MoneyValue valueToAdd);

    /**
     * Attempt to remove the given value from the money holder.
     * @return The amount of money left to be removed by another handler
     */
    @Nonnull
    MoneyValue tryRemoveMoney(@Nonnull MoneyValue valueToRemove);

    /**
     * Adds text to the tooltip detailing the contents of this money holder.<br>
     * Typically formatted as:<code><br>Title<br>Contents 1<br>Contents 2<br>etc.</code>
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

    /**
     * Easy implementation of {@link IMoneyHolder} that simply points to another parent money holder.
     * Typically used by things such as {@link io.github.lightman314.lightmanscurrency.common.bank.reference.BankReference}, etc.
     */
    abstract class Slave implements IMoneyHolder
    {

        @Nullable
        protected abstract IMoneyHolder getParent();

        @Nonnull
        @Override
        public final MoneyValue tryAddMoney(@Nonnull MoneyValue valueToAdd) {
            IMoneyHolder holder = this.getParent();
            if(holder != null)
                return holder.tryAddMoney(valueToAdd);
            return valueToAdd;
        }

        @Nonnull
        @Override
        public final MoneyValue tryRemoveMoney(@Nonnull MoneyValue valueToRemove) {
            IMoneyHolder holder = this.getParent();
            if(holder != null)
                return holder.tryRemoveMoney(valueToRemove);
            return valueToRemove;
        }

        @Override
        public void formatTooltip(@Nonnull List<Component> tooltip) {
            IMoneyHolder holder = this.getParent();
            if(holder != null)
                holder.formatTooltip(tooltip);
        }

        @Override
        public Component getTooltipTitle() { return EasyText.empty(); }

        @Nonnull
        @Override
        public final MoneyView getStoredMoney() {
            IMoneyHolder holder = this.getParent();
            if(holder != null)
                return holder.getStoredMoney();
            return MoneyView.empty();
        }

        @Override
        public final boolean hasStoredMoneyChanged(@Nullable Object context) {
            IMoneyHolder holder = this.getParent();
            if(holder != null)
                return holder.hasStoredMoneyChanged(context);
            return false;
        }

        @Override
        public final void flagAsKnown(@Nullable Object context) {
            IMoneyHolder holder = this.getParent();
            if(holder != null)
                holder.flagAsKnown(context);
        }

        @Override
        public final void forgetContext(@Nonnull Object context) {
            IMoneyHolder holder = this.getParent();
            if(holder != null)
                holder.forgetContext(context);
        }
    }

}
