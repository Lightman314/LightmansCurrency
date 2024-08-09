package io.github.lightman314.lightmanscurrency.common.menus.teams.tabs;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.team.TeamSalaryPaymentsClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TeamManagementMenu;
import io.github.lightman314.lightmanscurrency.common.menus.teams.TeamManagementTab;
import io.github.lightman314.lightmanscurrency.common.teams.Team;

import javax.annotation.Nonnull;

public class TeamSalaryPaymentsTab extends TeamManagementTab.BankManagement {

    public TeamSalaryPaymentsTab(@Nonnull TeamManagementMenu menu) { super(menu); }

    @Nonnull
    @Override
    protected AccessLevel accessLevel() { return AccessLevel.ADMINS; }

    @Nonnull
    @Override
    public Object createClientTab(@Nonnull Object screen) { return new TeamSalaryPaymentsClientTab(screen,this); }

    public void MakeSalaryMoneyCreative(boolean creative)
    {
        if(this.isClient())
            this.menu.SendMessage(this.builder().setBoolean("CreativeSalary",creative));
        else if(this.menu.selectedTeam() instanceof Team team)
            team.setSalaryMoneyCreative(this.menu.player,creative);
    }

    public void SetMemberSalary(@Nonnull MoneyValue value)
    {
        if(this.isClient())
            this.menu.SendMessage(this.builder().setMoneyValue("MemberSalary",value));
        else if(this.menu.selectedTeam() instanceof Team team)
            team.setMemberSalary(this.menu.player,value);
    }

    public void SetAdminSalarySeperate(boolean adminSalarySeperate)
    {
        if(this.isClient())
            this.menu.SendMessage(this.builder().setBoolean("AdminSalarySeperate",adminSalarySeperate));
        else if(this.menu.selectedTeam() instanceof Team team)
            team.setAdminSalarySeperate(this.menu.player,adminSalarySeperate);
    }

    public void SetAdminSalary(@Nonnull MoneyValue value)
    {
        if(this.isClient())
            this.menu.SendMessage(this.builder().setMoneyValue("AdminSalary",value));
        else if(this.menu.selectedTeam() instanceof Team team)
            team.setAdminSalary(this.menu.player,value);
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("CreativeSalary"))
            this.MakeSalaryMoneyCreative(message.getBoolean("CreativeSalary"));
        if(message.contains("MemberSalary"))
            this.SetMemberSalary(message.getMoneyValue("MemberSalary"));
        if(message.contains("AdminSalarySeperate"))
            this.SetAdminSalarySeperate(message.getBoolean("AdminSalarySeperate"));
        if(message.contains("AdminSalary"))
            this.SetAdminSalary(message.getMoneyValue("AdminSalary"));
    }

}