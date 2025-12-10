package io.github.lightman314.lightmanscurrency.common.teams;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.salary.CustomTarget;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TeamBankAccount extends BankAccount {

    private final ImmutableMap<String,CustomTarget> bonusOptions;

    public static final String TARGET_MEMBERS = "members";
    public static final String TARGET_ADMINS = "admins";

    public TeamBankAccount(ITeam team,Runnable markDirty) {
        super(markDirty);
        this.bonusOptions = ImmutableMap.copyOf(this.buildOptions(team));
    }
    public TeamBankAccount(ITeam team,Runnable markDirty,CompoundTag data,HolderLookup.Provider lookup) {
        super(markDirty,data,lookup);
        this.bonusOptions = ImmutableMap.copyOf(this.buildOptions(team));
    }

    private ImmutableMap<String,CustomTarget> buildOptions(ITeam team)
    {
        ImmutableMap.Builder<String, CustomTarget> builder = ImmutableMap.builderWithExpectedSize(2);
        builder.put(TARGET_MEMBERS,new MembersOnlyTarget(team));
        builder.put(TARGET_ADMINS,new AdminsTarget(team));
        return builder.buildKeepingLast();
    }

    @Override
    public Map<String,CustomTarget> extraSalaryTargets() { return this.bonusOptions; }

    private record MembersOnlyTarget(ITeam team) implements CustomTarget.ForPlayers
    {
        @Override
        public boolean isClient() {return this.team.isClient(); }
        @Override
        public List<PlayerReference> getPlayers() { return this.team.getMembers(); }
        @Override
        public Component getName() { return LCText.GUI_TEAM_SALARY_TARGET_MEMBERS.get(); }
    }

    private record AdminsTarget(ITeam team) implements CustomTarget.ForPlayers
    {
        @Override
        public boolean isClient() { return this.team.isClient();}
        @Override
        public List<PlayerReference> getPlayers() { return this.team.getAdminsAndOwner(); }
        @Override
        public Component getName() { return LCText.GUI_TEAM_SALARY_TARGET_ADMINS.get(); }
    }

}
