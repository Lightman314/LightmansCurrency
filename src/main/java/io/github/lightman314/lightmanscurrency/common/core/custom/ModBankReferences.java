package io.github.lightman314.lightmanscurrency.common.core.custom;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReferenceType;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.*;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBankReferences {

    public static final DeferredRegister<BankReferenceType<?>> REGISTER = DeferredRegister.create(LCRegistries.BANK_REFERENCE,LightmansCurrency.MODID);

    static {
        REGISTER.register("personal",() -> PlayerBankReference.TYPE);
        REGISTER.register("team_account",() -> TeamBankReference.TYPE);
    }

}
