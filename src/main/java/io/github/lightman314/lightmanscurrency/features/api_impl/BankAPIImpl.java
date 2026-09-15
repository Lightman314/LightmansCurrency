package io.github.lightman314.lightmanscurrency.features.api_impl;

import io.github.lightman314.lightmanscurrency.api.bank_account.BankAPI;
import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.bank_account.source.BankAccountSource;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;

import java.util.*;

public final class BankAPIImpl implements BankAPI {

    public static final BankAPIImpl INSTANCE = new BankAPIImpl();
    private BankAPIImpl() {}

    private final Set<BankAccountSource> sources = new HashSet<>();

    @Override
    public void registerBankAccountSource(BankAccountSource source) { this.sources.add(source); }

    @Override
    public List<BankAccount> getAllBankAccounts(ISidedContext context) {
        List<BankAccount> accounts = new ArrayList<>();
        this.sources.forEach(source -> accounts.addAll(source.collectAllBankAccounts(context)));
        return accounts;
    }

    @Override
    public List<BankReference> getAllBankReferences(ISidedContext context) {
        List<BankReference> accounts = new ArrayList<>();
        this.sources.forEach(source -> accounts.addAll(source.collectAllReferences(context)));
        return accounts;
    }

}
