package io.github.lightman314.lightmanscurrency.integration.ftbteams.ownership;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamManager;
import dev.ftb.mods.ftbteams.api.client.ClientTeamManager;
import dev.ftb.mods.ftbteams.data.AbstractTeamBase;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.IPotentialOwnerProvider;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.PotentialOwner;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.*;

public class FTBTeamOwnerProvider implements IPotentialOwnerProvider {

    @Nonnull
    @Override
    public List<PotentialOwner> collectPotentialOwners(@Nonnull Player player) {
        UUID playerID = player.getUUID();
        List<PotentialOwner> results = new ArrayList<>();
        for(Team team : this.allTeams(player.level().isClientSide))
        {
            if(team instanceof AbstractTeamBase t)
            {
                if(t.isMember(playerID) && !t.isPlayerTeam())
                    results.add(new PotentialFTBTeamOwner(t.getTeamId()));
            }
        }
        return results;
    }

    private Collection<Team> allTeams(boolean isClient)
    {
        if(isClient && FTBTeamsAPI.api().isClientManagerLoaded())
        {
            ClientTeamManager manager = FTBTeamsAPI.api().getClientManager();
            return manager.getTeams();
        }
        else if(FTBTeamsAPI.api().isManagerLoaded())
        {
            TeamManager manager = FTBTeamsAPI.api().getManager();
            return manager.getTeams();
        }
        return Collections.emptyList();
    }
}
