package io.github.lightman314.lightmanscurrency.integration.ftbteams;

import io.github.lightman314.lightmanscurrency.api.ownership.OwnershipAPI;
import io.github.lightman314.lightmanscurrency.integration.ftbteams.ownership.FTBTeamOwner;
import io.github.lightman314.lightmanscurrency.integration.ftbteams.ownership.FTBTeamOwnerProvider;
import net.neoforged.fml.ModList;

public class LCFTBTeams {

    public static boolean isLoaded() { return ModList.get().isLoaded("ftbteams"); }

    public static void setup()
    {
        OwnershipAPI.getApi().registerOwnerType(FTBTeamOwner.TYPE);
        OwnershipAPI.getApi().registerPotentialOwnerProvider(new FTBTeamOwnerProvider());
    }

}
