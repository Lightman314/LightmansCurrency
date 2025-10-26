package io.github.lightman314.lightmanscurrency.api.teams;

import io.github.lightman314.lightmanscurrency.common.impl.TeamAPIImpl;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Comparator;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class TeamAPI {

    private static TeamAPI instance;
    public static TeamAPI getApi()
    {
        if(instance == null)
            instance = new TeamAPIImpl();
        return instance;
    }

    protected TeamAPI() { if(instance != null)  throw new IllegalCallerException("Cannot create a new TeamAPI instance as one is already present!"); }

    /**
     * Gets the team with the given team id.<br>
     * Works on both the logical client &amp; server depending on your <code>isClient</code> input.
     */
    @Nullable
    public abstract ITeam GetTeam(boolean isClient, long teamID);
    /**
     * Gets the team with the given team id.<br>
     * Works on both the logical client &amp; server depending on whether the {@link IClientTracker context} is client-side or not
     */
    @Nullable
    public final ITeam GetTeam(IClientTracker context, long teamID) { return this.GetTeam(context.isClient(),teamID); }


    /**
     * Gets a list of all teams on the server.<br>
     * Works on both the logical client &amp; server depending on your <code>isClient</code> input.
     */
    public abstract List<ITeam> GetAllTeams(boolean isClient);
    /**
     * Gets a list of all teams on the server.<br>
     * Works on both the logical client &amp; server depending on whether the {@link IClientTracker context} is client-side or not
     */
    public final List<ITeam> GetAllTeams(IClientTracker context) { return this.GetAllTeams(context.isClient()); }


    /**
     * Gets a list of all teams that the player is a member of, and sorts them using {@link #SorterForPlayer(Player)}.<br>
     * Works on both the logical client &amp; server.
     */
    public abstract List<ITeam> GetAllTeamsForPlayer(Player player);

    /**
     * Creates a new team with the given owner &amp; team name.<br>
     * Should only be run on the logical server. If run on the logical client, <code>null</code> will be returned instead.
     * @return The {@link ITeam} that was created.
     */
    @Nullable
    public abstract ITeam CreateTeam(Player owner, String name);

    /**
     * The Team list sorter for the given player.<br>
     * This sorter prioritizes teams that they own above those that they are only admins/members of,
     * and prioritizes teams that they are admins on above those that they are only members of.
     */
    public abstract Comparator<ITeam> SorterForPlayer(Player player);

}