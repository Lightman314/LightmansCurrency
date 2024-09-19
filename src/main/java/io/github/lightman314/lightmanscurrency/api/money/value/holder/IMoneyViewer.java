package io.github.lightman314.lightmanscurrency.api.money.value.holder;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;

import javax.annotation.Nonnull;

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

}
