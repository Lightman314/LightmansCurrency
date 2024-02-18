package io.github.lightman314.lightmanscurrency.api.money.value.holder;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Base for the {@link io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler IMoneyHandler} and {@link IMoneyHolder} interfaces.<br>
 * Only used for read-only access to a money containers contents.<br>
 * Use {@link MoneyViewer} for a template implementation of this interface that caches money results to avoid unnecessary calculations.
 */
public interface IMoneyViewer {

    /**
     * The latest known value of the money available in this viewer.
     */
    @Nonnull
    MoneyView getStoredMoney();

    /**
     * Whether the results of {@link #getStoredMoney()} has changed since the last time it was queried by the given object.
     * If <code>context</code> is null, it only returns <code>true</code> if the stored money has changed for <i>any</i> context.
     */
    boolean hasStoredMoneyChanged(@Nullable Object context);

    /**
     * Flags the current results of {@link #getStoredMoney()} as known for the given object.
     */
    void flagAsKnown(@Nullable Object context);

    /**
     * Flags the current results of {@link #getStoredMoney()} as unknown for the given object.
     * Used to clear the list of known states for objects that will no longer query this viewer.
     */
    void forgetContext(@Nonnull Object context);

}
