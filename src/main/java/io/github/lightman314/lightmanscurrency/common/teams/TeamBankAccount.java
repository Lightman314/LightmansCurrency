package io.github.lightman314.lightmanscurrency.common.teams;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.salary.CustomTarget;
import io.github.lightman314.lightmanscurrency.api.money.bank.salary.SalaryData;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.builtin.MoneyStorage;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Map;

public class TeamBankAccount extends BankAccount {

    public static final Codec<TeamBankAccount> CODEC = buildCodec(TeamBankAccount::new);
    public static final StreamCodec<RegistryFriendlyByteBuf,TeamBankAccount> STREAM_CODEC = buildStreamCodec(TeamBankAccount.class,TeamBankAccount::new);

    private ImmutableMap<String,CustomTarget> bonusOptions = ImmutableMap.of();

    public static final String TARGET_MEMBERS = "members";
    public static final String TARGET_ADMINS = "admins";

    public TeamBankAccount forTeam(ITeam team) { this.bonusOptions = ImmutableMap.copyOf(this.buildOptions(team)); return this; }

    public TeamBankAccount() { }
    protected TeamBankAccount(MoneyStorage money, NotificationData logger, String ownerName, Map<String, MoneyValue> notificaionLevels, int cardValidation, List<SalaryData> salaries) { super(money,logger,ownerName,notificaionLevels,cardValidation,salaries); }

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
