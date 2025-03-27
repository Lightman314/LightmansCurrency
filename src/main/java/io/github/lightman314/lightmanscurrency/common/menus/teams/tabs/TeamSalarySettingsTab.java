package io.github.lightman314.lightmanscurrency.common.menus.teams.tabs;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.client.gui.screen.team.TeamSalarySettingsClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TeamManagementMenu;
import io.github.lightman314.lightmanscurrency.common.menus.teams.TeamManagementTab;
import io.github.lightman314.lightmanscurrency.common.teams.Team;

import javax.annotation.Nonnull;

public class TeamSalarySettingsTab extends TeamManagementTab.BankManagement {

    public TeamSalarySettingsTab(@Nonnull TeamManagementMenu menu) { super(menu); }

    @Nonnull
    @Override
    protected AccessLevel accessLevel() { return AccessLevel.ADMINS; }

    @Nonnull
    @Override
    public Object createClientTab(@Nonnull Object screen) { return new TeamSalarySettingsClientTab(screen,this); }

    public void SetAutoSalary(boolean enabled)
    {
        if(this.menu.isClient())
            this.menu.SendMessage(this.builder().setBoolean("EnableAutoSalary",enabled));
        else if(this.menu.selectedTeam() instanceof Team team)
            team.setAutoSalaryEnabled(this.menu.player,enabled);
    }

    public void SetSalaryNotification(boolean enabled)
    {
        if(this.menu.isClient())
            this.menu.SendMessage(this.builder().setBoolean("SalaryNotification",enabled));
        else if(this.menu.selectedTeam() instanceof Team team)
            team.setSalaryNotification(this.menu.player,enabled);
    }

    public void SetSalaryDelay(long delay)
    {
        if(this.menu.isClient())
            this.menu.SendMessage(this.builder().setLong("SalaryDelay",delay));
        else if(this.menu.selectedTeam() instanceof Team team)
            team.setSalaryDelay(this.menu.player,delay);
    }

    public void SetSalaryLoginRequirement(boolean loginRequired)
    {
        if(this.menu.isClient())
            this.menu.SendMessage(this.builder().setBoolean("LoginRequirement",loginRequired));
        else if(this.menu.selectedTeam() instanceof Team team)
            team.setLoginRequiredForSalary(this.menu.player,loginRequired);
    }

    public void ManuallyTriggerSalary()
    {
        if(this.menu.isClient())
            this.menu.SendMessage(this.builder().setFlag("TriggerSalary"));
        else
        {
            ITeam team = this.menu.selectedTeam();
            if(team == null || !team.isAdmin(this.menu.player))
                return;
            team.forcePaySalaries(false);
        }
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("EnableAutoSalary"))
            this.SetAutoSalary(message.getBoolean("EnableAutoSalary"));
        if(message.contains("SalaryNotification"))
            this.SetSalaryNotification(message.getBoolean("SalaryNotification"));
        if(message.contains("SalaryDelay"))
            this.SetSalaryDelay(message.getLong("SalaryDelay"));
        if(message.contains("LoginRequirement"))
            this.SetSalaryLoginRequirement(message.getBoolean("LoginRequirement"));
        if(message.contains("TriggerSalary"))
            this.ManuallyTriggerSalary();
    }

}