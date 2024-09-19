package io.github.lightman314.lightmanscurrency.api.money.value.holder;

import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class MoneyViewer implements IMoneyViewer {

    @Nonnull
    @Override
    public final MoneyView getStoredMoney() {
        MoneyView.Builder builder = MoneyView.builder();
        this.collectStoredMoney(builder);
        return builder.build();
    }

    protected abstract void collectStoredMoney(@Nonnull MoneyView.Builder builder);

    /**
     * Easy implementation of {@link IMoneyHolder} that simply points to another parent money holder.
     * Typically used by things such as {@link BankReference}, etc.
     */
    public static abstract class Slave implements IMoneyViewer
    {
        @Nullable
        abstract IMoneyViewer getParent();

        @Nonnull
        @Override
        public final MoneyView getStoredMoney() {
            IMoneyViewer holder = this.getParent();
            if(holder != null)
                return holder.getStoredMoney();
            return MoneyView.empty();
        }
    }

}
