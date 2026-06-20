package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.coins.atm.commands.ATMCommandType;
import io.github.lightman314.lightmanscurrency.api.coins.atm.commands.builtin.*;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCATMCommandTypes {

    private LCATMCommandTypes() {}

    public static final DeferredRegister<ATMCommandType<?>> REGISTER = DeferredRegister.create(LCRegistries.Coins.ATM_COMMAND_TYPE,LCApi.MODID);

    static {
        register("exchange_all",ExchangeAllCommand.TYPE);
        register("exchange", ExchangeCommand.TYPE);
    }

    private static void register(String name,ATMCommandType<?> type) {
        REGISTER.register(name,() -> type);
    }

}