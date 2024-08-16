package io.github.lightman314.lightmanscurrency.api.ownership.listing.builtin;

import io.github.lightman314.lightmanscurrency.api.ownership.listing.IPotentialOwnerProvider;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.PotentialOwner;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.api.teams.TeamAPI;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TeamOwnerProvider implements IPotentialOwnerProvider {

    public static final IPotentialOwnerProvider INSTANCE = new TeamOwnerProvider();

    private TeamOwnerProvider() {}

    @Nonnull
    @Override
    public List<PotentialOwner> collectPotentialOwners(@Nonnull Player player) {
        List<PotentialOwner> results = new ArrayList<>();
        for(ITeam team : TeamAPI.API.GetAllTeams(player.level().isClientSide))
        {
            if(team.isMember(player))
            {
                PotentialTeamOwner owner = new PotentialTeamOwner(team.getID());
                if(team.isOwner(player))
                    owner.addPriority(2);
                if(team.isAdmin(player))
                    owner.addPriority(1);
                results.add(owner);
            }
        }
        return results;
    }
}
