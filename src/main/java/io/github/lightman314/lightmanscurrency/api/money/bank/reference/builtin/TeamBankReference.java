package io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin;

import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReferenceType;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.api.teams.TeamAPI;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TeamBankReference extends BankReference {

    public static final BankReferenceType TYPE = new Type();

    public final long teamID;
    protected TeamBankReference(long teamID) { super(TYPE); this.teamID = teamID; }

    public static BankReference of(long teamID) { return new TeamBankReference(teamID); }
    public static BankReference of(ITeam team) {
        BankReference br = of(team.getID());
        br.flagAsClient(team.isClient());
        return br; }

    @Nullable
    @Override
    public IconData getIcon() { return IconUtil.ICON_ALEX_HEAD; }

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

    @Override
    protected void saveAdditional(CompoundTag tag) { tag.putLong("TeamID", this.teamID); }

    @Override
    protected void encodeAdditional(FriendlyByteBuf buffer) { buffer.writeLong(this.teamID); }

    private static class Type extends BankReferenceType
    {
        protected Type() { super(VersionUtil.lcResource( "team_account")); }

        @Override
        public BankReference load(CompoundTag tag) { return of(tag.getLong("TeamID")); }

        @Override
        public BankReference decode(FriendlyByteBuf buffer) { return of(buffer.readLong()); }
    }
}
