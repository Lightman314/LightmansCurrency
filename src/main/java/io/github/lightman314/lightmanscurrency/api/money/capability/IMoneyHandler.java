package io.github.lightman314.lightmanscurrency.api.money.capability;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;

/**
 * Base
 */
public interface IMoneyHandler extends IMoneyViewer {

    /**
     * Attempts to insert money into the money handler.
     * @param insertAmount The {@link MoneyValue} to insert into this money handler.
     * @param simulation Whether this is a simulation. When <code>true</code> no money will actually be stored.
     * @return The {@link MoneyValue} that could <b>not</b> be inserted. If nothing was inserted, it will match the <code>insertAmount</code> input.<br>
     * If the full amount was inserted, the result of {@link MoneyValue#isEmpty()} will be <code>true</code>.
     */
    MoneyValue insertMoney(MoneyValue insertAmount, boolean simulation);

    /**
     * Attempts to take money from the money handler.
     * @param extractAmount The {@link MoneyValue} to take from this money handler.
     * @param simulation Whether this is a simulation. When <code>true</code> no money will actually be extracted.
     * @return The {@link MoneyValue} that could <b>not</b> be extracted. If nothing was extracted, it will match the <code>extractAmount</code> input.<br>
     * If the full amount was extracted, the result of {@link MoneyValue#isEmpty()} will be <code>true</code>.
     */
    MoneyValue extractMoney(MoneyValue extractAmount, boolean simulation);

    /**
     * Whether the given {@link MoneyValue} of that type can be accepted by this money handler.<br>
     * If <code>false</code>, {@link #insertMoney(MoneyValue, boolean)} will always return the <code>insertAmount</code> argument.
     */
    boolean isMoneyTypeValid(MoneyValue value);

}
