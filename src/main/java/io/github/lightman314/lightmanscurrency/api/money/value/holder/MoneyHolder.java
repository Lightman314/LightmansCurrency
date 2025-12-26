package io.github.lightman314.lightmanscurrency.api.money.value.holder;

import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.capability.money.MoneyHandler;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class MoneyHolder extends MoneyHandler implements IMoneyHolder {

    public static final IMoneyHolder EMPTY = new Empty();

    public static IMoneyHolder createFromHandler(IMoneyHandler handler, Component tooltipTitle, int priority) { return new HandlerSlave(handler, tooltipTitle, priority); }
    public static IMoneyHolder createFromHandler(IMoneyHandler handler, Component tooltipTitle, int priority, int inversePriority) { return new HandlerSlave(handler, tooltipTitle, priority, inversePriority); }

    /**
     * Easy implementation of {@link IMoneyHolder} that simply points to another parent money holder.
     * Typically used by things such as {@link BankReference}, etc.
     */
    public static abstract class Slave extends MoneyViewer.Slave implements IMoneyHolder
    {

        @Nullable
        protected abstract IMoneyHolder getParent();

        @Override
        public void formatTooltip(List<Component> tooltip) {
            IMoneyHolder holder = this.getParent();
            if(holder != null)
                holder.formatTooltip(tooltip);
        }

        @Override
        public Component getTooltipTitle() { return EasyText.empty(); }
        
        @Override
        public MoneyValue insertMoney(MoneyValue insertAmount, boolean simulation) {
            IMoneyHolder holder = this.getParent();
            if(holder != null)
                return holder.insertMoney(insertAmount, simulation);
            return insertAmount;
        }
        
        @Override
        public MoneyValue extractMoney(MoneyValue extractAmount, boolean simulation) {
            IMoneyHolder holder = this.getParent();
            if(holder != null)
                return holder.extractMoney(extractAmount, simulation);
            return extractAmount;
        }

        @Override
        public boolean isMoneyTypeValid(MoneyValue value) {
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
        private final int inversePriority;
        private HandlerSlave(IMoneyHandler handler, Component title, int priority) { this(handler,title,priority,priority * -1); }
        private HandlerSlave(IMoneyHandler handler, Component title, int priority, int inversePriority) {
            this.handler = handler;
            this.title = title;
            this.priority = priority;
            this.inversePriority = inversePriority;
        }

        @Override
        public int priority() { return this.priority; }
        @Override
        public int inversePriority() { return this.inversePriority; }

        @Override
        public Component getTooltipTitle() { return this.title; }
        
        @Override
        public MoneyValue insertMoney(MoneyValue insertAmount, boolean simulation) { return this.handler.insertMoney(insertAmount,simulation); }
        
        @Override
        public MoneyValue extractMoney(MoneyValue extractAmount, boolean simulation) { return this.handler.extractMoney(extractAmount,simulation); }
        @Override
        public boolean isMoneyTypeValid(MoneyValue value) { return this.handler.isMoneyTypeValid(value); }
        
        @Override
        public MoneyView getStoredMoney() { return this.handler.getStoredMoney(); }
    }

    private static class Empty implements IMoneyHolder
    {
        @Override
        public void formatTooltip(List<Component> tooltip) { }
        @Override
        public Component getTooltipTitle() { return EasyText.empty(); }
        
        @Override
        public MoneyView getStoredMoney() { return MoneyView.empty(); }
        
        @Override
        public MoneyValue insertMoney(MoneyValue insertAmount, boolean simulation) { return insertAmount; }
        
        @Override
        public MoneyValue extractMoney(MoneyValue extractAmount, boolean simulation) { return extractAmount; }
        @Override
        public boolean isMoneyTypeValid(MoneyValue value) { return false; }
    }


}
