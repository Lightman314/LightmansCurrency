package io.github.lightman314.lightmanscurrency.common.menus.teams.tabs;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.client.gui.screen.team.TeamOwnerClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TeamManagementMenu;
import io.github.lightman314.lightmanscurrency.common.menus.teams.TeamManagementTab;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;

import javax.annotation.Nonnull;

public class TeamOwnerTab extends TeamManagementTab.Management {

    public TeamOwnerTab(@Nonnull TeamManagementMenu menu) { super(menu); }

    @Nonnull
    @Override
    public Object createClientTab(@Nonnull Object screen) { return new TeamOwnerClientTab(screen,this); }

    @Nonnull
    @Override
    protected AccessLevel accessLevel() { return AccessLevel.OWNER; }

    public void SetOwner(@Nonnull String ownerName)
    {
        if(this.isClient())
            this.menu.SendMessage(this.builder().setString("SetOwner",ownerName));
        else if(this.menu.selectedTeam() instanceof Team team)
            team.changeOwner(this.menu.player,ownerName);
    }

    public void DisbandTeam()
    {
        if(this.isClient())
            this.menu.SendMessage(this.builder().setFlag("DisbandTeam"));
        else
        {
            ITeam team = this.menu.selectedTeam();
            if(team != null && team.isOwner(this.menu.player))
                TeamSaveData.RemoveTeam(team.getID());
        }
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("SetOwner"))
            this.SetOwner(message.getString("SetOwner"));
        if(message.contains("DisbandTeam"))
            this.DisbandTeam();
    }

}