package io.github.lightman314.lightmanscurrency.common.menus.teams.tabs;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.client.gui.screen.team.TeamNameClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TeamManagementMenu;
import io.github.lightman314.lightmanscurrency.common.menus.teams.TeamManagementTab;
import io.github.lightman314.lightmanscurrency.common.teams.Team;

import javax.annotation.Nonnull;

public class TeamNameTab extends TeamManagementTab.Management {

    public TeamNameTab(@Nonnull TeamManagementMenu menu) { super(menu); }

    @Nonnull
    @Override
    protected AccessLevel accessLevel() { return AccessLevel.ADMINS; }

    @Nonnull
    @Override
    public Object createClientTab(@Nonnull Object screen) { return new TeamNameClientTab(screen,this); }

    public void ChangeName(@Nonnull String newName)
    {
        ITeam team = this.menu.selectedTeam();
        if(team instanceof Team t)
            t.changeName(this.menu.player,newName);
        if(this.isClient())
            this.menu.SendMessage(this.builder().setString("ChangeName",newName));

    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("ChangeName"))
            this.ChangeName(message.getString("ChangeName"));
    }

}