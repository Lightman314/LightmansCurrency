package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.BankReferenceType;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.builtin.*;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCBankReferenceTypes {
    private LCBankReferenceTypes() {}

    public static final DeferredRegister<BankReferenceType<?>> REGISTER = DeferredRegister.create(LCRegistries.Bank.REFERENCE_TYPE,LCApi.MODID);

    static {
        register("player", PlayerBankReference.TYPE);
    }

    private static void register(String name,BankReferenceType<?> type) {
        REGISTER.register(name,() -> type);
    }

}