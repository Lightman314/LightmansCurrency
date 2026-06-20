package io.github.lightman314.lightmanscurrency.api.bank_account;

import io.github.lightman314.lightmanscurrency.api.bank_account.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;

import java.util.List;

public interface BankAPI {

    default List<BankAccount> getAllBankAccounts(boolean isClient) { return this.getAllBankAccounts(ISidedContext.known(isClient)); }
    List<BankAccount> getAllBankAccounts(ISidedContext context);
    default List<BankReference> getAllBankReferences(boolean isClient) { return this.getAllBankReferences(ISidedContext.known(isClient)); }
    List<BankReference> getAllBankReferences(ISidedContext context);

}