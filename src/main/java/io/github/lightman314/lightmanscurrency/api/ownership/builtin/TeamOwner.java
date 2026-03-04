package io.github.lightman314.lightmanscurrency.api.ownership.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
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
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TeamOwner extends Owner {

    public static final OwnerType<TeamOwner> TYPE = new Type();

    public final long teamID;
    @Nullable
    public final ITeam getTeam() { return TeamAPI.getApi().GetTeam(this, this.teamID); }
    private TeamOwner(long teamID) { this.teamID = teamID; }
    public static TeamOwner of(ITeam team) { return of(team.getID()); }
    public static TeamOwner of(long teamID) { return new TeamOwner(teamID); }

    @Override
    public Component getName() {
        ITeam team = this.getTeam();
        if(team != null)
            return EasyText.literal(team.getName());
        return EasyText.literal("NULL");
    }
    
    @Override
    public Component getCommandLabel() { return LCText.COMMAND_LCADMIN_DATA_OWNER_TEAM.get(this.getName(), this.teamID); }

    @Override
    public boolean stillValid() { return TeamAPI.getApi().GetTeam(this, this.teamID) != null; }

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
    public boolean isAdmin(PlayerReference player) {
        ITeam team = this.getTeam();
        if(team != null)
            return team.isAdmin(player);
        return false;
    }

    @Override
    public boolean isMember(PlayerReference player) {
        ITeam team = this.getTeam();
        if(team != null)
            return team.isMember(player);
        return false;
    }

    
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
    public void pushNotification(Supplier<? extends Notification> notificationSource, int notificationLevel, boolean sendToChat) {
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
                NotificationAPI.getApi().PushPlayerNotification(player.id,notificationSource.get(),sendToChat);
        }
    }

    @Override
    public <T> void incrementStat(StatKey<?, T> key, T addValue) {
        ITeam team = this.getTeam();
        if(team != null)
            team.getStats().incrementStat(key,addValue);
    }

    @Override
    public OwnerType<?> getType() { return TYPE; }
    
    @Override
    public Owner copy() { return new TeamOwner(this.teamID); }

    @Override
    public boolean matches(Owner other) { return other instanceof TeamOwner to && to.teamID == this.teamID; }

    @Override
    public int hash() { return Long.hashCode(this.teamID); }

    private static class Type extends OwnerType<TeamOwner>
    {
        private static final MapCodec<TeamOwner> MAP_CODEC = Codec.LONG.fieldOf("team")
                .xmap(TeamOwner::new,o -> o.teamID);
        private static final StreamCodec<ByteBuf,TeamOwner> STREAM_CODEC = ByteBufCodecs.VAR_LONG
                .map(TeamOwner::new,o -> o.teamID);
        @Override
        public Owner loadOldData(CompoundTag tag, HolderLookup.Provider lookup) { return of(tag.getLong("Team")); }
        @Override
        public MapCodec<TeamOwner> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TeamOwner> streamCodec() { return STREAM_CODEC; }
    }

}
