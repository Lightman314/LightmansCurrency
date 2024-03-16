package io.github.lightman314.lightmanscurrency.api.money.bank.source.builtin;

import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.TeamBankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.source.BankAccountSource;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.api.teams.TeamAPI;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TeamBankAccountSource extends BankAccountSource {

    public static final BankAccountSource INSTANCE = new TeamBankAccountSource();

    private TeamBankAccountSource() {}

    @Nonnull
    @Override
    public List<BankReference> CollectAllReferences(boolean isClient) {
        List<BankReference> list = new ArrayList<>();
        for(ITeam team : TeamAPI.getAllTeams(isClient))
        {
            if(team.hasBankAccount())
                list.add(TeamBankReference.of(team).flagAsClient(isClient));
        }
        return list;
    }
}
