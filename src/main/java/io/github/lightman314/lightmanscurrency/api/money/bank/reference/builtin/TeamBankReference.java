package io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReferenceType;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.api.teams.TeamAPI;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public class TeamBankReference extends BankReference {

    public static final BankReferenceType<TeamBankReference> TYPE = new Type();

    private static final MapCodec<TeamBankReference> MAP_CODEC = Codec.LONG.xmap(TeamBankReference::new, br -> br.teamID).fieldOf("team");
    private static final StreamCodec<ByteBuf,TeamBankReference> STREAM_CODEC = ByteBufCodecs.VAR_LONG.map(TeamBankReference::new, br -> br.teamID);

    public final long teamID;
    protected TeamBankReference(long teamID) { this.teamID = teamID; }

    public static BankReference of(long teamID) { return new TeamBankReference(teamID); }
    public static BankReference of(ITeam team) {
        BankReference br = of(team.getID());
        br.flagAsClient(team.isClient());
        return br;
    }

    @Nullable
    @Override
    public IconData getIcon() { return IconUtil.ICON_ALEX_HEAD; }

    @Override
    public BankReferenceType<?> getType() { return TYPE; }

    @Nullable
    @Override
    public IBankAccount get() {
        ITeam team = TeamAPI.getApi().GetTeam(this, this.teamID);
        if(team != null)
            return team.getBankAccount();
        return null;
    }

    @Override
    public boolean isSalaryTarget(PlayerReference player) {
        ITeam team = TeamAPI.getApi().GetTeam(this,this.teamID);
        if(team != null && team.hasBankAccount())
            return team.isMember(player);
        return false;
    }

    @Override
    public boolean allowedAccess(PlayerReference player) {
        ITeam team = TeamAPI.getApi().GetTeam(this, this.teamID);
        if(team != null && team.hasBankAccount())
            return team.canAccessBankAccount(player);
        return false;
    }

    @Override
    public boolean allowedAccess(Player player) {
        ITeam team = TeamAPI.getApi().GetTeam(this, this.teamID);
        if(team != null && team.hasBankAccount())
            return team.canAccessBankAccount(player);
        return false;
    }

    @Override
    public int salaryPermission(PlayerReference player) {
        ITeam team = TeamAPI.getApi().GetTeam(this, this.teamID);
        if(team != null && team.hasBankAccount())
            return team.getSalaryLevel(player);
        return 0;
    }

    private static class Type extends BankReferenceType<TeamBankReference>
    {
        @Override
        public MapCodec<TeamBankReference> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<ByteBuf,TeamBankReference> streamCodec() { return STREAM_CODEC; }
        @Override
        public BankReference loadOldData(CompoundTag tag) { return of(tag.getLong("TeamID")); }
    }
}
