package io.github.lightman314.lightmanscurrency.api.money.capability;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Expansion of the {@link MoneyViewer} template class, but forcing {@link IMoneyHandler} implementation.
 */
public abstract class MoneyHandler extends MoneyViewer implements IMoneyHandler {

    public static IMoneyHandler combine(List<IMoneyHandler> handlers) { return new MultiMoneyHandler(handlers); }

    /**
     * Basic implem
     */
    public static abstract class Slave implements IMoneyHandler
    {

        @Nullable
        protected abstract IMoneyHandler getParent();
        
        @Override
        public MoneyValue insertMoney(MoneyValue insertAmount, boolean simulation) {
            IMoneyHandler handler = this.getParent();
            if(handler != null)
                return handler.insertMoney(insertAmount,simulation);
            return insertAmount;
        }

        @Override
        public MoneyValue extractMoney(MoneyValue extractAmount, boolean simulation) {
            IMoneyHandler handler = this.getParent();
            if(handler != null)
                return handler.extractMoney(extractAmount,simulation);
            return extractAmount;
        }

        @Override
        public boolean isMoneyTypeValid(MoneyValue value) {
            IMoneyHandler handler = this.getParent();
            if(handler != null)
                return handler.isMoneyTypeValid(value);
            return false;
        }

        
        @Override
        public MoneyView getStoredMoney() {
            IMoneyHandler handler = this.getParent();
            if(handler != null)
                return handler.getStoredMoney();
            return MoneyView.empty();
        }
    }

    private static class MultiMoneyHandler extends MoneyHandler
    {

        private final List<IMoneyHandler> handlers;
        MultiMoneyHandler(List<IMoneyHandler> handlers) { this.handlers = handlers; }

        
        @Override
        public MoneyValue insertMoney(MoneyValue insertAmount, boolean simulation) {
            for(IMoneyHandler h : this.handlers)
            {
                if(h.isMoneyTypeValid(insertAmount))
                {
                    insertAmount = h.insertMoney(insertAmount, simulation);
                    if(insertAmount.isEmpty())
                        return MoneyValue.empty();
                }
            }
            return insertAmount;
        }

        
        @Override
        public MoneyValue extractMoney(MoneyValue extractAmount, boolean simulation) {
            for(IMoneyHandler h : this.handlers)
            {
                extractAmount = h.extractMoney(extractAmount, simulation);
                if(extractAmount.isEmpty())
                    return MoneyValue.empty();
            }
            return extractAmount;
        }

        @Override
        public boolean isMoneyTypeValid(MoneyValue value) { return this.handlers.stream().anyMatch(h -> h.isMoneyTypeValid(value)); }

        @Override
        protected void collectStoredMoney(MoneyView.Builder builder) {
            for(IMoneyHandler h : this.handlers)
                builder.merge(h.getStoredMoney());
        }
    }

}
