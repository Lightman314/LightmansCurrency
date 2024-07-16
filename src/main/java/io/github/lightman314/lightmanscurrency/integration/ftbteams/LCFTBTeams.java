package io.github.lightman314.lightmanscurrency.integration.ftbteams;

import io.github.lightman314.lightmanscurrency.api.ownership.OwnershipAPI;
import io.github.lightman314.lightmanscurrency.integration.ftbteams.ownership.FTBTeamOwner;
import io.github.lightman314.lightmanscurrency.integration.ftbteams.ownership.FTBTeamOwnerProvider;
import net.minecraftforge.fml.ModList;

public class LCFTBTeams {

    public static boolean isLoaded() { return ModList.get().isLoaded("ftbteams"); }

    public static void setup()
    {
        OwnershipAPI.API.registerOwnerType(FTBTeamOwner.TYPE);
        OwnershipAPI.API.registerPotentialOwnerProvider(new FTBTeamOwnerProvider());
    }

}
