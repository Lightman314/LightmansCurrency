package io.github.lightman314.lightmanscurrency.integration.ftbteams.ownership;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamManager;
import dev.ftb.mods.ftbteams.api.TeamRank;
import dev.ftb.mods.ftbteams.api.client.ClientTeamManager;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationAPI;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerType;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class FTBTeamOwner extends Owner {

    public static final OwnerType TYPE = OwnerType.create(VersionUtil.lcResource("ftbteams"),(tag) -> new FTBTeamOwner(tag.getUUID("Team")));

    private final UUID teamID;
    public FTBTeamOwner(@Nonnull UUID teamID) { this.teamID = teamID; }

    @Nullable
    private Team getTeam()
    {
        if(this.isClient() && FTBTeamsAPI.api().isClientManagerLoaded())
        {
            ClientTeamManager manager = FTBTeamsAPI.api().getClientManager();
            return manager.getTeamByID(this.teamID).orElse(null);
        }
        else if(FTBTeamsAPI.api().isManagerLoaded())
        {
            TeamManager manager = FTBTeamsAPI.api().getManager();
            return manager.getTeamByID(this.teamID).orElse(null);
        }
        return null;
    }

    @Nonnull
    @Override
    public MutableComponent getName() {
        Team team = this.getTeam();
        if(team != null)
            return EasyText.makeMutable(team.getName()).setStyle(Style.EMPTY);
        return LCText.GUI_OWNER_NULL.get();
    }

    @Nonnull
    @Override
    public MutableComponent getCommandLabel() { return LCText.COMMAND_LCADMIN_DATA_OWNER_TEAM.get(this.getName(),this.teamID); }

    @Override
    public boolean stillValid() {
        Team team = this.getTeam();
        return team != null && !team.isPlayerTeam();
    }

    @Override
    public boolean isOnline() {
        Team team = this.getTeam();
        if(team != null)
            return !team.getOnlineMembers().isEmpty();
        return false;
    }

    @Override
    public boolean isAdmin(@Nonnull PlayerReference player) {
        Team team = this.getTeam();
        if(team != null)
        {
            Map<UUID,TeamRank> ranks = team.getPlayersByRank(TeamRank.OFFICER);
            return ranks.containsKey(player.id);
        }
        return false;
    }

    @Override
    public boolean isMember(@Nonnull PlayerReference player) {
        Team team = this.getTeam();
        if(team != null)
        {
            Map<UUID,TeamRank> ranks = team.getPlayersByRank(TeamRank.MEMBER);
            return ranks.containsKey(player.id);
        }
        return false;
    }

    @Nonnull
    @Override
    public PlayerReference asPlayerReference() {
        Team team = this.getTeam();
        if(team != null)
            return PlayerReference.of(team.getOwner(),"").copyWithName(team.getName().getString());
        return PlayerReference.NULL;
    }

    @Nullable
    @Override
    public BankReference asBankReference() { return null; }

    @Override
    public void pushNotification(@Nonnull Supplier<? extends Notification> notificationSource, int notificationLevel, boolean sendToChat) {
        Team team = this.getTeam();
        if(team != null)
        {
            TeamRank minRank = TeamRank.OWNER;
            if(notificationLevel < 1)
                minRank = TeamRank.MEMBER;
            else if(notificationLevel < 2)
                minRank = TeamRank.OFFICER;
            team.getPlayersByRank(minRank).forEach((player,rank) ->
                NotificationAPI.getApi().PushPlayerNotification(player,notificationSource.get(),sendToChat)
            );
        }
    }

    @Nonnull
    @Override
    public OwnerType getType() { return TYPE; }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        tag.putUUID("Team", this.teamID);
    }

    @Nonnull
    @Override
    public Owner copy() { return new FTBTeamOwner(this.teamID); }

    @Override
    public boolean matches(@Nonnull Owner other) {
        if(other instanceof FTBTeamOwner to)
            return to.teamID.equals(this.teamID);
        return false;
    }
    
}
