package io.github.lightman314.lightmanscurrency.common.menus.teams.tabs;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.client.gui.screen.team.TeamBankAccountClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TeamManagementMenu;
import io.github.lightman314.lightmanscurrency.common.menus.teams.TeamManagementTab;
import io.github.lightman314.lightmanscurrency.common.teams.Team;

import javax.annotation.Nonnull;

public class TeamBankAccountTab extends TeamManagementTab.Management {

    public TeamBankAccountTab(@Nonnull TeamManagementMenu menu) { super(menu); }

    @Nonnull
    @Override
    public Object createClientTab(@Nonnull Object screen) { return new TeamBankAccountClientTab(screen,this); }

    @Nonnull
    @Override
    protected AccessLevel accessLevel() { return AccessLevel.OWNER; }

    public void CreateBankAccount() {
        if(this.isClient())
            this.menu.SendMessage(this.builder().setFlag("CreateBankAccount"));
        else if(this.menu.selectedTeam() instanceof Team team)
            team.createBankAccount(this.menu.player);
    }

    public void ChangeBankAccess(int level)
    {
        if(this.isClient())
            this.menu.SendMessage(this.builder().setInt("ChangeBankAccess",level));
        else if(this.menu.selectedTeam() instanceof Team team)
            team.changeBankLimit(this.menu.player,Owner.validateNotificationLevel(level));
    }

    public void ChangeSalaryAccess(int level)
    {
        if(this.isClient())
            this.menu.SendMessage(this.builder().setInt("ChangeSalaryAccess",level));
        else if(this.menu.selectedTeam() instanceof Team team)
            team.changeSalaryLimit(this.menu.player,Owner.validateNotificationLevel(level));
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("CreateBankAccount"))
            this.CreateBankAccount();
        if(message.contains("ChangeBankAccess"))
            this.ChangeBankAccess(message.getInt("ChangeBankAccess"));
        if(message.contains("ChangeSalaryAccess"))
            this.ChangeSalaryAccess(message.getInt("ChangeSalaryAccess"));
    }

}
