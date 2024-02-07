package io.github.lightman314.lightmanscurrency.integration.claiming.flan;

import io.github.lightman314.lightmanscurrency.integration.claiming.LCClaiming;

public class LCFlanIntegration {

    public static void setup() { LCClaiming.setup(LCFlanClaimHandler.INSTANCE); }
}
