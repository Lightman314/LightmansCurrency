package io.github.lightman314.lightmanscurrency.api.money.capability;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;

/**
 * Base for the {@link IMoneyHandler IMoneyHandler} and {@link IMoneyHolder} interfaces.<br>
 * Only used for read-only access to a money containers contents.<br>
 * Use {@link MoneyViewer} for a template implementation of this interface that caches money results to avoid unnecessary calculations.
 */
public interface IMoneyViewer {

    /**
     * The latest known value of the money available in this viewer.
     */
    MoneyView getStoredMoney();

}
