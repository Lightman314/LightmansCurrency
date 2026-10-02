package io.github.lightman314.lightmanscurrency.api.client.gui.widget.bank;

import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;

import javax.annotation.Nullable;

public interface IBankInteractionHandler {

    @Nullable
    BankAccount getBankAccount();

    void attemptDeposit(MoneyValue amount);
    void attemptWithdraw(MoneyValue amount);

}