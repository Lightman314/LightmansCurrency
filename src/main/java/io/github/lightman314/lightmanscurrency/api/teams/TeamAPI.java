package io.github.lightman314.lightmanscurrency.api.teams;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class TeamAPI {

    private TeamAPI(){}

    /**
     * Gets the team with the given team id.<br>
     * Works on both the logical client & server depending on your <code>isClient</code> input.
     */
    @Nullable
    public static ITeam getTeam(boolean isClient, long teamID) { return TeamSaveData.GetTeam(isClient,teamID); }

    /**
     * Gets a list of all teams on the server.
     * Works on both the logical client & server depending on your <code>isClient</code> input.
     */
    @Nonnull
    public static List<? extends ITeam> getAllTeams(boolean isClient) { return TeamSaveData.GetAllTeams(isClient); }

    /**
     * Gets a list of all teams that the player is a member of, and sorts them using {@link #sorterFor(Player)}.
     * Works on both the logical client & server.
     */
    @Nonnull
    public static List<ITeam> getAllTeamsForPlayer(@Nonnull Player player)
    {
        List<ITeam> result = new ArrayList<>();
        List<? extends ITeam> allTeams = getAllTeams(player.level().isClientSide);
        for(ITeam team : allTeams)
        {
            if(team.isMember(player))
                result.add(team);
        }
        result.sort(sorterFor(player));
        return ImmutableList.copyOf(result);
    }


    /**
     * Creates a new team with the given owner & team name.<br>
     * Should only be run on the logical server. If run on the logical client, <code>null</code> will be returned instead.
     * @return The {@link ITeam} that was created.
     */
    @Nullable
    public static ITeam createTeam(@Nonnull Player owner, @Nonnull String name)
    {
        if(owner.level().isClientSide)
            return null;
        return TeamSaveData.RegisterTeam(owner,name);
    }

    /**
     * The Team list sorter for the given player.<br>
     * This sorter prioritizes teams that they own above those that they are only admins/members of,
     * and prioritizes teams that they are admins on above those that they are only members of.
     */
    @Nonnull
    public static Comparator<ITeam> sorterFor(@Nonnull Player player) { return Team.sorterFor(player); }

}