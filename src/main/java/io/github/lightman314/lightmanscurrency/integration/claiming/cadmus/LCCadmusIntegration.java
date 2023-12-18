package io.github.lightman314.lightmanscurrency.integration.claiming.cadmus;

import io.github.lightman314.lightmanscurrency.integration.claiming.LCClaiming;
import io.github.lightman314.lightmanscurrency.integration.claiming.bonus_data.LCBonusClaimHandler;

public class LCCadmusIntegration {

    public static void setup() {
        LCClaiming.setup(LCBonusClaimHandler.INSTANCE);
        LCMaxClaimProvider.register();
    }

}
