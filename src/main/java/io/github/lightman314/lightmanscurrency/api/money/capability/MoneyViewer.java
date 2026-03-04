package io.github.lightman314.lightmanscurrency.api.money.capability;

import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import org.jetbrains.annotations.Nullable;

public abstract class MoneyViewer implements IMoneyViewer {
    
    @Override
    public final MoneyView getStoredMoney() {
        MoneyView.Builder builder = MoneyView.builder();
        this.collectStoredMoney(builder);
        return builder.build();
    }

    protected abstract void collectStoredMoney(MoneyView.Builder builder);

    /**
     * Easy implementation of {@link IMoneyHolder} that simply points to another parent money holder.
     * Typically used by things such as {@link BankReference}, etc.
     */
    public static abstract class Slave implements IMoneyViewer
    {
        @Nullable
        abstract IMoneyViewer getParent();
        @Override
        public final MoneyView getStoredMoney() {
            IMoneyViewer holder = this.getParent();
            if(holder != null)
                return holder.getStoredMoney();
            return MoneyView.empty();
        }
    }

}
