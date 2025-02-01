package io.github.lightman314.lightmanscurrency.api.money.bank.source.builtin;

import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.source.BankAccountSource;
import io.github.lightman314.lightmanscurrency.common.data.types.BankDataCache;

import javax.annotation.Nonnull;
import java.util.List;

public class PlayerBankAccountSource extends BankAccountSource {

    public static final BankAccountSource INSTANCE = new PlayerBankAccountSource();

    private PlayerBankAccountSource() {}

    @Nonnull
    @Override
    public List<BankReference> CollectAllReferences(boolean isClient) { return BankDataCache.TYPE.get(isClient).getPlayerBankAccounts(); }

}
