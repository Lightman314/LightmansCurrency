package io.github.lightman314.lightmanscurrency.common.menus.teams.tabs;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.team.TeamMemberListClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TeamManagementMenu;
import io.github.lightman314.lightmanscurrency.common.menus.teams.TeamManagementTab;

import javax.annotation.Nonnull;

public class TeamMemberListTab extends TeamManagementTab.Management {

    public TeamMemberListTab(@Nonnull TeamManagementMenu menu) { super(menu); }

    @Nonnull
    @Override
    public Object createClientTab(@Nonnull Object screen) { return new TeamMemberListClientTab(screen,this); }
    @Nonnull
    @Override
    protected AccessLevel accessLevel() { return AccessLevel.MEMBERS; }
    @Override
    public void receiveMessage(LazyPacketData message) {}

}
