package io.github.lightman314.lightmanscurrency.api.teams;

import io.github.lightman314.lightmanscurrency.common.impl.TeamAPIImpl;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public abstract class TeamAPI {

    public static final TeamAPI API = TeamAPIImpl.INSTANCE;

    /**
     * Gets the team with the given team id.<br>
     * Works on both the logical client & server depending on your <code>isClient</code> input.
     */
    @Nullable
    public abstract ITeam GetTeam(boolean isClient, long teamID);
    /**
     * Gets the team with the given team id.<br>
     * Works on both the logical client & server depending on whether the {@link IClientTracker context} is client-side or not
     */
    @Nullable
    public final ITeam GetTeam(@Nonnull IClientTracker context, long teamID) { return this.GetTeam(context.isClient(),teamID); }


    /**
     * @deprecated Use {@link #GetTeam(boolean, long)} instead
     * @see #API
     * @see #GetTeam(IClientTracker, long)
     */
    @Deprecated(since = "2.2.3.1")
    @Nullable
    public static ITeam getTeam(boolean isClient, long teamID) { return API.GetTeam(isClient,teamID); }


    /**
     * Gets a list of all teams on the server.<br>
     * Works on both the logical client & server depending on your <code>isClient</code> input.
     */
    @Nonnull
    public abstract List<ITeam> GetAllTeams(boolean isClient);
    /**
     * Gets a list of all teams on the server.<br>
     * Works on both the logical client & server depending on whether the {@link IClientTracker context} is client-side or not
     */
    @Nonnull
    public final List<ITeam> GetAllTeams(@Nonnull IClientTracker context) { return this.GetAllTeams(context.isClient()); }

    /**
     * Use {@link #GetAllTeams(boolean)} instead
     * @see #API
     * @see #GetAllTeams(IClientTracker)
     */
    @Nonnull
    @Deprecated(since = "2.2.3.1")
    public static List<? extends ITeam> getAllTeams(boolean isClient) { return API.GetAllTeams(isClient); }


    /**
     * Gets a list of all teams that the player is a member of, and sorts them using {@link #sorterFor(Player)}.<br>
     * Works on both the logical client & server.
     */
    @Nonnull
    public abstract List<ITeam> GetAllTeamsForPlayer(@Nonnull Player player);

    /**
     * @deprecated Use {@link #GetAllTeamsForPlayer(Player)} instead
     * @see #API
     */
    @Deprecated(since = "2.2.3.1")
    @Nonnull
    public static List<ITeam> getAllTeamsForPlayer(@Nonnull Player player) { return API.GetAllTeamsForPlayer(player); }

    /**
     * Creates a new team with the given owner & team name.<br>
     * Should only be run on the logical server. If run on the logical client, <code>null</code> will be returned instead.
     * @return The {@link ITeam} that was created.
     */
    @Nullable
    public abstract ITeam CreateTeam(@Nonnull Player owner, @Nonnull String name);


    /**
     * @deprecated Use {@link #CreateTeam(Player, String)} instead
     * @see #API
     */
    @Deprecated(since = "2.2.3.1")
    @Nullable
    public static ITeam createTeam(@Nonnull Player owner, @Nonnull String name) { return API.CreateTeam(owner,name); }

    /**
     * The Team list sorter for the given player.<br>
     * This sorter prioritizes teams that they own above those that they are only admins/members of,
     * and prioritizes teams that they are admins on above those that they are only members of.
     */
    @Nonnull
    public abstract Comparator<ITeam> SorterForPlayer(@Nonnull Player player);

    /**
     * @deprecated Use {@link #SorterForPlayer(Player)} instead.
     * @see #API
     */
    @Deprecated(since = "2.2.3.1")
    @Nonnull
    public static Comparator<ITeam> sorterFor(@Nonnull Player player) { return Team.sorterFor(player); }

}