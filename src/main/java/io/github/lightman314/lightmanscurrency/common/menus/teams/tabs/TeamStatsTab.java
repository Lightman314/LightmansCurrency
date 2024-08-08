package io.github.lightman314.lightmanscurrency.common.menus.teams.tabs;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.team.TeamStatsClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TeamManagementMenu;
import io.github.lightman314.lightmanscurrency.common.menus.teams.TeamManagementTab;
import io.github.lightman314.lightmanscurrency.common.teams.Team;

import javax.annotation.Nonnull;

public class TeamStatsTab extends TeamManagementTab.Management {

    public TeamStatsTab(@Nonnull TeamManagementMenu menu) { super(menu); }

    @Nonnull
    @Override
    public Object createClientTab(@Nonnull Object screen) { return new TeamStatsClientTab(screen,this); }

    @Nonnull
    @Override
    protected AccessLevel accessLevel() { return AccessLevel.MEMBERS; }

    public void ClearStats(boolean fullClear) {
        if(this.isClient())
            this.menu.SendMessage(this.builder().setBoolean("ClearStats",fullClear));
        else if(this.menu.selectedTeam() instanceof Team team)
            team.clearStats(this.menu.player, fullClear);
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("ClearStats"))
            this.ClearStats(message.getBoolean("ClearStats"));
    }

}
