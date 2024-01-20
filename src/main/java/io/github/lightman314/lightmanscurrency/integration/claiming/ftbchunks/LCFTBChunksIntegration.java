package io.github.lightman314.lightmanscurrency.integration.claiming.ftbchunks;

import io.github.lightman314.lightmanscurrency.integration.claiming.LCClaiming;

public class LCFTBChunksIntegration {

    public static void setup() { LCClaiming.setup(LCFTBClaimHandler.INSTANCE); }

}
