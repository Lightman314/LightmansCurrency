package io.github.lightman314.lightmanscurrency.common.impl;

import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.api.teams.TeamAPI;
import io.github.lightman314.lightmanscurrency.common.data.types.TeamDataCache;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class TeamAPIImpl extends TeamAPI {

    public static final TeamAPI INSTANCE = new TeamAPIImpl();

    private TeamAPIImpl() {}

    @Nullable
    @Override
    public ITeam GetTeam(boolean isClient, long teamID) {
        TeamDataCache data = TeamDataCache.TYPE.get(isClient);
        if(data == null)
            return null;
        return data.getTeam(teamID);
    }

    @Nonnull
    @Override
    public List<ITeam> GetAllTeams(boolean isClient) {
        TeamDataCache data = TeamDataCache.TYPE.get(isClient);
        if(data == null)
            return new ArrayList<>();
        return data.getAllTeams();
    }

    @Nonnull
    @Override
    public List<ITeam> GetAllTeamsForPlayer(@Nonnull Player player) {
        List<ITeam> result = new ArrayList<>();
        for(ITeam team : this.GetAllTeams(player.level().isClientSide))
        {
            if(team.isMember(player))
                result.add(team);
        }
        result.sort(this.SorterForPlayer(player));
        return new ArrayList<>(result);
    }

    @Nullable
    @Override
    public ITeam CreateTeam(@Nonnull Player owner, @Nonnull String name) {
        if(owner.level().isClientSide)
            return null;
        TeamDataCache data = TeamDataCache.TYPE.get(false);
        if(data == null)
            return null;
        return data.registerTeam(owner,name);
    }

    @Nonnull
    @Override
    public Comparator<ITeam> SorterForPlayer(@Nonnull Player player) { return Team.sorterFor(player); }

}
