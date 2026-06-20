package io.github.lightman314.lightmanscurrency.features.api_impl;

import io.github.lightman314.lightmanscurrency.api.bank_account.BankAPI;
import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;

import java.util.List;

public final class BankAPIImpl implements BankAPI {

    public static final BankAPIImpl INSTANCE = new BankAPIImpl();
    private BankAPIImpl() {}

    @Override
    public List<BankAccount> getAllBankAccounts(ISidedContext context) {
        return List.of();
    }

    @Override
    public List<BankReference> getAllBankReferences(ISidedContext context) {
        return List.of();
    }
}
