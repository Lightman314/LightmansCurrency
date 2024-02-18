package io.github.lightman314.lightmanscurrency.api.money.value.holder;

import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.capability.money.MoneyHandler;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class MoneyHolder extends MoneyHandler implements IMoneyHolder {

    public static final IMoneyHolder EMPTY = new Empty();

    @Nonnull
    public static IMoneyHolder createFromHandler(@Nonnull IMoneyHandler handler, @Nonnull Component tooltipTitle, int priority) { return new HandlerSlave(handler, tooltipTitle, priority); }

    /**
     * Easy implementation of {@link IMoneyHolder} that simply points to another parent money holder.
     * Typically used by things such as {@link BankReference}, etc.
     */
    public static abstract class Slave extends MoneyViewer.Slave implements IMoneyHolder
    {

        @Nullable
        protected abstract IMoneyHolder getParent();

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
        public MoneyValue insertMoney(@Nonnull MoneyValue insertAmount, boolean simulation) {
            IMoneyHolder holder = this.getParent();
            if(holder != null)
                return holder.insertMoney(insertAmount, simulation);
            return insertAmount;
        }

        @Nonnull
        @Override
        public MoneyValue extractMoney(@Nonnull MoneyValue extractAmount, boolean simulation) {
            IMoneyHolder holder = this.getParent();
            if(holder != null)
                return holder.extractMoney(extractAmount, simulation);
            return extractAmount;
        }

        @Override
        public boolean isMoneyTypeValid(@Nonnull MoneyValue value) {
            IMoneyHolder holder = this.getParent();
            if(holder != null)
                return holder.isMoneyTypeValid(value);
            return false;
        }
    }

    private static class HandlerSlave implements IMoneyHolder
    {

        private final IMoneyHandler handler;
        private final Component title;
        private final int priority;
        private HandlerSlave(@Nonnull IMoneyHandler handler, @Nonnull Component title, int priority) {
            this.handler = handler;
            this.title = title;
            this.priority = priority;
        }

        @Override
        public int priority() { return this.priority; }
        @Override
        public Component getTooltipTitle() { return this.title; }
        @Nonnull
        @Override
        public MoneyValue insertMoney(@Nonnull MoneyValue insertAmount, boolean simulation) { return this.handler.insertMoney(insertAmount,simulation); }
        @Nonnull
        @Override
        public MoneyValue extractMoney(@Nonnull MoneyValue extractAmount, boolean simulation) { return this.handler.extractMoney(extractAmount,simulation); }
        @Override
        public boolean isMoneyTypeValid(@Nonnull MoneyValue value) { return this.handler.isMoneyTypeValid(value); }
        @Nonnull
        @Override
        public MoneyView getStoredMoney() { return this.handler.getStoredMoney(); }
        @Override
        public boolean hasStoredMoneyChanged(@Nullable Object context) { return this.handler.hasStoredMoneyChanged(context); }
        @Override
        public void flagAsKnown(@Nullable Object context) { this.handler.flagAsKnown(context); }
        @Override
        public void forgetContext(@Nonnull Object context) { this.handler.forgetContext(context); }
    }

    private static class Empty implements IMoneyHolder
    {
        @Override
        public void formatTooltip(@Nonnull List<Component> tooltip) { }
        @Override
        public Component getTooltipTitle() { return EasyText.empty(); }
        @Nonnull
        @Override
        public MoneyView getStoredMoney() { return MoneyView.empty(); }
        @Override
        public boolean hasStoredMoneyChanged(@Nullable Object context) { return false; }
        @Override
        public void flagAsKnown(@Nullable Object context) { }
        @Override
        public void forgetContext(@Nonnull Object context) { }
        @Nonnull
        @Override
        public MoneyValue insertMoney(@Nonnull MoneyValue insertAmount, boolean simulation) { return insertAmount; }
        @Nonnull
        @Override
        public MoneyValue extractMoney(@Nonnull MoneyValue extractAmount, boolean simulation) { return extractAmount; }
        @Override
        public boolean isMoneyTypeValid(@Nonnull MoneyValue value) { return false; }
    }


}
