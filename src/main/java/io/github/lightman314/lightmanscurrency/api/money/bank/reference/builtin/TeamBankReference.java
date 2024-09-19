package io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReferenceType;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.api.teams.TeamAPI;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TeamBankReference extends BankReference {

    public static final BankReferenceType TYPE = new Type();

    public final long teamID;
    protected TeamBankReference(long teamID) { super(TYPE); this.teamID = teamID; }

    public static BankReference of(long teamID) { return new TeamBankReference(teamID); }
    public static BankReference of(@Nonnull ITeam team) {
        BankReference br = of(team.getID());
        br.flagAsClient(team.isClient());
        return br; }

    @Nullable
    @Override
    public IconData getIcon() { return IconUtil.ICON_ALEX_HEAD; }

    @Nullable
    @Override
    public IBankAccount get() {
        ITeam team = TeamAPI.API.GetTeam(this, this.teamID);
        if(team != null)
            return team.getBankAccount();
        return null;
    }

    @Override
    public boolean allowedAccess(@Nonnull PlayerReference player) {
        ITeam team = TeamAPI.API.GetTeam(this, this.teamID);
        if(team != null && team.hasBankAccount())
            return team.canAccessBankAccount(player);
        return false;
    }

    @Override
    public boolean allowedAccess(@Nonnull Player player) {
        ITeam team = TeamSaveData.GetTeam(this.isClient(), this.teamID);
        if(team != null && team.hasBankAccount())
            return team.canAccessBankAccount(player);
        return false;
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) { tag.putLong("TeamID", this.teamID); }

    @Override
    protected void encodeAdditional(@Nonnull FriendlyByteBuf buffer) { buffer.writeLong(this.teamID); }

    private static class Type extends BankReferenceType
    {
        protected Type() { super(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "team_account")); }

        @Override
        public BankReference load(CompoundTag tag) { return of(tag.getLong("TeamID")); }

        @Override
        public BankReference decode(FriendlyByteBuf buffer) { return of(buffer.readLong()); }
    }
}
