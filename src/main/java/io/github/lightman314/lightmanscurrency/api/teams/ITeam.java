package io.github.lightman314.lightmanscurrency.api.teams;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface ITeam extends IClientTracker {

    long getID();

    @Nonnull
    String getName();

    /**
     * The {@link PlayerReference Player} who owns the team.<br>
     * Does not use {@link OwnerData OwnerData} for ownership as teams cannot be owned by another team.
     */
    @Nonnull
    PlayerReference getOwner();
    /**
     * List of <i>only</i> the team admins.<br>
     * Does not include the owner. For a list of all players who have admin abilities see {@link #getAdminsAndOwner()}
     */
    @Nonnull
    List<PlayerReference> getAdmins();
    /**
     * List of <i>only</i> the teams normal members.<br>
     * Does not include the admins or the owner. For a list of all players who have member abilities see {@link #getAllMembers()}
     */
    @Nonnull
    List<PlayerReference> getMembers();

    /**
     * List of the teams admins including the owner.
     * For a list of admins without the owner, see {@link #getAdmins()}
     */
    @Nonnull
    default List<PlayerReference> getAdminsAndOwner()
    {
        List<PlayerReference> result = new ArrayList<>(this.getAdmins());
        result.add(this.getOwner());
        return ImmutableList.copyOf(result);
    }

    /**
     * List of the teams members, admins, and the owner.
     */
    @Nonnull
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
    boolean canAccessBankAccount(@Nonnull Player player);

    /**
     * The bank account stored in this teams data.<br>
     * Make sure to check {@link #canAccessBankAccount(Player)} or {@link io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.TeamBankReference#allowedAccess(Player) TeamBankReference#allowedAccess(Player)} before attempting any player-based interactions.
     */
    @Nullable
    IBankAccount getBankAccount();

    /**
     * Determines if the given player is the owner of this team.<br>
     * Also returns true if the player is in admin mode.
     */
    boolean isOwner(@Nonnull Player player);
    /**
     * Determines if the given player reference is the owner of this team.
     */
    default boolean isOwner(@Nonnull PlayerReference player) { return this.isOwner(player.id); }
    /**
     * Determines if the player with the given id is the owner of this team.
     */
    boolean isOwner(@Nonnull UUID playerID);
    /**
     * Determines if the given player is an admin or owner of this team.<br>
     * Also returns true if the player is in admin mode.
     */
    boolean isAdmin(@Nonnull Player player);
    /**
     * Determines if the given player reference is an admin or owner of this team.
     */
    default boolean isAdmin(@Nonnull PlayerReference player) { return this.isAdmin(player.id); }
    /**
     * Determines if the player with the given id is an admin or owner of this team.
     */
    boolean isAdmin(@Nonnull UUID playerID);
    /**
     * Determines if the given player is a member, admin, or owner of this team.
     * Also returns true if the player is in admin mode.
     */
    boolean isMember(@Nonnull Player player);
    /**
     * Determines if the given player reference is a member, admin, or owner of this team.
     */
    default boolean isMember(@Nonnull PlayerReference player) { return this.isMember(player.id); }
    /**
     * Determines if the player with the given id is a member, admin, or owner of this team.
     */
    boolean isMember(@Nonnull UUID playerID);


}
