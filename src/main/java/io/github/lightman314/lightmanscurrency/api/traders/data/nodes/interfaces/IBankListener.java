package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;

public interface IBankListener {

    default void afterBankLink(IBankAccount account) {}
    default void afterBankUnlink() {}

}
