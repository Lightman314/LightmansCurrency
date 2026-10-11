package io.github.lightman314.lightmanscurrency.api.bank_account.source.builtin;

import io.github.lightman314.lightmanscurrency.api.bank_account.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.bank_account.source.BankAccountSource;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.features.api_impl.data.PlayerBankDataCache;

import java.util.List;

public final class PlayerBankSource extends BankAccountSource {

    public static final PlayerBankSource INSTANCE = new PlayerBankSource();

    private PlayerBankSource() {}

    @Override
    public List<BankReference> collectAllReferences(ISidedContext context) {
        PlayerBankDataCache data = PlayerBankDataCache.TYPE.get(context);
        if(data == null)
            return List.of();
        return data.getPlayerBankAccounts();
    }

}