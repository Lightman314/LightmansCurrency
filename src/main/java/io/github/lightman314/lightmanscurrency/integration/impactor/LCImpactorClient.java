package io.github.lightman314.lightmanscurrency.integration.impactor;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.money.client.ClientMoneyAPI;
import io.github.lightman314.lightmanscurrency.integration.impactor.money.client.ClientImpactorType;

public class LCImpactorClient {

    public static void setupClient()
    {
        if(LCConfig.COMMON.compatImpactor.get())
            ClientMoneyAPI.getApi().RegisterClientType(ClientImpactorType.INSTANCE);
    }

}