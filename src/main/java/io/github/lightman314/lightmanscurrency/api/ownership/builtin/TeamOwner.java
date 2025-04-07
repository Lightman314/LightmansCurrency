package io.github.lightman314.lightmanscurrency.api.ownership.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.TeamBankReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationAPI;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerType;
import io.github.lightman314.lightmanscurrency.api.stats.StatKey;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.api.teams.TeamAPI;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TeamOwner extends Owner {

    public static final OwnerType TYPE = OwnerType.create(VersionUtil.lcResource("team"),
            (tag,lookup) -> of(tag.getLong("Team")));

    public final long teamID;
    @Nullable
    public final ITeam getTeam() { return TeamAPI.API.GetTeam(this, this.teamID); }
    private TeamOwner(long teamID) { this.teamID = teamID; }
    @Nonnull
    public static TeamOwner of(@Nonnull ITeam team) { return of(team.getID()); }
    @Nonnull
    public static TeamOwner of(long teamID) { return new TeamOwner(teamID); }

    @Nonnull
    @Override
    public MutableComponent getName() {
        ITeam team = this.getTeam();
        if(team != null)
            return EasyText.literal(team.getName());
        return EasyText.literal("NULL");
    }

    @Nonnull
    @Override
    public MutableComponent getCommandLabel() { return LCText.COMMAND_LCADMIN_DATA_OWNER_TEAM.get(this.getName(), this.teamID); }

    @Override
    public boolean stillValid() { return TeamAPI.API.GetTeam(this, this.teamID) != null; }

    @Override
    public boolean isOnline() {
        if(this.isClient())
            return false;
        ITeam team = this.getTeam();
        if(team != null)
        {
            for(PlayerReference member : team.getAllMembers())
            {
                if(member != null && member.isOnline())
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAdmin(@Nonnull PlayerReference player) {
        ITeam team = this.getTeam();
        if(team != null)
            return team.isAdmin(player);
        return false;
    }

    @Override
    public boolean isMember(@Nonnull PlayerReference player) {
        ITeam team = this.getTeam();
        if(team != null)
            return team.isMember(player);
        return false;
    }

    @Nonnull
    @Override
    public PlayerReference asPlayerReference() {
        ITeam team = this.getTeam();
        if(team != null)
            return team.getOwner().copyWithName(team.getName());
        return PlayerReference.NULL;
    }

    @Nullable
    @Override
    public BankReference asBankReference() {
        ITeam team = this.getTeam();
        if(team != null && team.hasBankAccount())
            return TeamBankReference.of(team);
        return null;
    }

    @Override
    public boolean hasNotificationLevels() { return true; }

    @Override
    public void pushNotification(@Nonnull Supplier<? extends Notification> notificationSource, int notificationLevel, boolean sendToChat) {
        ITeam team = this.getTeam();
        if(team == null)
            return;
        List<PlayerReference> sendTo = new ArrayList<>();
        if(notificationLevel < 1)
            sendTo.addAll(team.getMembers());
        if(notificationLevel < 2)
            sendTo.addAll(team.getAdmins());
        sendTo.add(team.getOwner());
        for(PlayerReference player: sendTo)
        {
            if(player != null && player.id != null)
                NotificationAPI.API.PushPlayerNotification(player.id,notificationSource.get(),sendToChat);
        }
    }

    @Override
    public <T> void incrementStat(@Nonnull StatKey<?, T> key, @Nonnull T addValue) {
        ITeam team = this.getTeam();
        if(team != null)
            team.getStats().incrementStat(key,addValue);
    }

    @Nonnull
    @Override
    public OwnerType getType() { return TYPE; }
    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup) { tag.putLong("Team", this.teamID); }

    @Nonnull
    @Override
    public Owner copy() { return new TeamOwner(this.teamID); }

    @Override
    public boolean matches(@Nonnull Owner other) { return other instanceof TeamOwner to && to.teamID == this.teamID; }

}
