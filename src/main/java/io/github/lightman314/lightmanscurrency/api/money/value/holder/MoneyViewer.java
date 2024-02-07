package io.github.lightman314.lightmanscurrency.api.money.value.holder;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class MoneyViewer implements IMoneyViewer {

    private MoneyView cachedValue = null;
    private final List<Object> knowsLatest = new ArrayList<>();

    @Nonnull
    @Override
    public final MoneyView getStoredMoney() {
        if(this.cachedValue == null || this.hasStoredMoneyChanged())
        {
            MoneyView.Builder builder = MoneyView.builder();
            this.collectStoredMoney(builder);
            this.cachedValue = builder.build();
        }
        return this.cachedValue;
    }

    @Override
    public final boolean hasStoredMoneyChanged(@Nullable Object context) { return this.cachedValue == null || this.hasStoredMoneyChanged() || (context != null && !this.knowsLatest.contains(context));}

    protected abstract boolean hasStoredMoneyChanged();

    protected abstract void collectStoredMoney(@Nonnull MoneyView.Builder builder);

    @Override
    public final void flagAsKnown(@Nullable Object context) {
        if(context != null && !this.knowsLatest.contains(context))
            this.knowsLatest.add(context);
        this.onFlagAsKnown(context);
    }

    protected void onFlagAsKnown(@Nullable Object context) {}

    @Override
    public final void forgetContext(@Nonnull Object context) { this.knowsLatest.remove(context); }

}
