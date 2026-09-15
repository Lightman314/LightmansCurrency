package io.github.lightman314.lightmanscurrency.api.bank_account.source;

import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;

import java.util.List;
import java.util.Objects;

public abstract class BankAccountSource {

    public abstract List<BankReference> collectAllReferences(ISidedContext context);

    public List<BankAccount> collectAllBankAccounts(ISidedContext context) { return this.collectAllReferences(context).stream().map(BankReference::get).filter(Objects::nonNull).toList(); }

}
