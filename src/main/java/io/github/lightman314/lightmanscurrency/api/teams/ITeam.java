package io.github.lightman314.lightmanscurrency.api.teams;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.stats.StatTracker;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Range;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface ITeam extends IClientTracker {

    long getID();
    String getName();
    StatTracker getStats();

    /**
     * The {@link PlayerReference Player} who owns the team.<br>
     * Does not use {@link OwnerData OwnerData} for ownership as teams cannot be owned by another team.
     */
    PlayerReference getOwner();
    /**
     * List of <i>only</i> the team admins.<br>
     * Does not include the owner. For a list of all players who have admin abilities see {@link #getAdminsAndOwner()}
     */
    List<PlayerReference> getAdmins();
    /**
     * List of <i>only</i> the teams normal members.<br>
     * Does not include the admins or the owner. For a list of all players who have member abilities see {@link #getAllMembers()}
     */
    List<PlayerReference> getMembers();

    /**
     * List of the teams admins including the owner.
     * For a list of admins without the owner, see {@link #getAdmins()}
     */
    default List<PlayerReference> getAdminsAndOwner()
    {
        List<PlayerReference> result = new ArrayList<>(this.getAdmins());
        result.add(this.getOwner());
        return ImmutableList.copyOf(result);
    }

    /**
     * List of the teams members, admins, and the owner.
     */
    default List<PlayerReference> getAllMembers() {
        List<PlayerReference> result = new ArrayList<>();
        result.addAll(this.getMembers());
        result.addAll(this.getAdmins());
        result.add(this.getOwner());
        return ImmutableList.copyOf(result);
    }

    default int getMemberCount() { return this.getMembers().size() + this.getAdmins().size() + 1; }

    /**
     * Whether the team has a {@link io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount Bank Account} built for it.
     */
    boolean hasBankAccount();

    /**
     * Whether the given player is allowed to access the teams bank account.
     * Should also check {@link #hasBankAccount()} to confirm that one exists first.
     */
    boolean canAccessBankAccount(PlayerReference player);
    /**
     * Whether the given player is allowed to access the teams bank account.
     * Should also check {@link #hasBankAccount()} to confirm that one exists first.
     */
    boolean canAccessBankAccount(Player player);

    /**
     * Whether the given player is allowed to view the teams salaries
     * Should also check {@link #hasBankAccount()} to confirm that one exists first.
     */
    int getSalaryLevel(PlayerReference player);

    /**
     * An integer value indicating which players have access to the teams bank account<br>
     * Follows the same rules as the Owner's Notification Levels
     */
    @Range(from = 0, to = 2)
    int getBankLimit();

    @Range(from = 0, to = 2)
    int getBankSalaryEdit();

    /**
     * The bank account stored in this teams data.<br>
     * Make sure to check {@link #canAccessBankAccount(Player)} or {@link io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.TeamBankReference#allowedAccess(Player) TeamBankReference#allowedAccess(Player)} before attempting any player-based interactions.
     */
    @Nullable
    IBankAccount getBankAccount();

    /**
     * A {@link BankReference} that can be used to access this teams bank account.
     */
    @Nullable
    BankReference getBankReference();

    /**
     * Determines if the given player is the owner of this team.<br>
     * Also returns true if the player is in admin mode.
     */
    boolean isOwner(Player player);
    /**
     * Determines if the given player reference is the owner of this team.
     */
    default boolean isOwner(PlayerReference player) { return this.isOwner(player.id); }
    /**
     * Determines if the player with the given id is the owner of this team.
     */
    boolean isOwner(UUID playerID);
    /**
     * Determines if the given player is an admin or owner of this team.<br>
     * Also returns true if the player is in admin mode.
     */
    boolean isAdmin(Player player);
    /**
     * Determines if the given player reference is an admin or owner of this team.
     */
    default boolean isAdmin(PlayerReference player) { return this.isAdmin(player.id); }
    /**
     * Determines if the player with the given id is an admin or owner of this team.
     */
    boolean isAdmin(UUID playerID);
    /**
     * Determines if the given player is a member, admin, or owner of this team.
     * Also returns true if the player is in admin mode.
     */
    boolean isMember(Player player);
    /**
     * Determines if the given player reference is a member, admin, or owner of this team.
     */
    default boolean isMember(PlayerReference player) { return this.isMember(player.id); }
    /**
     * Determines if the player with the given id is a member, admin, or owner of this team.
     */
    boolean isMember(UUID playerID);


}