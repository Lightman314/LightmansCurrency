package io.github.lightman314.lightmanscurrency.common.menus.teams.tabs;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.team.TeamMemberEditClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TeamManagementMenu;
import io.github.lightman314.lightmanscurrency.common.menus.teams.TeamManagementTab;
import io.github.lightman314.lightmanscurrency.common.teams.Team;

import javax.annotation.Nonnull;

public class TeamMemberEditTab extends TeamManagementTab.Management {

    public TeamMemberEditTab(@Nonnull TeamManagementMenu menu) { super(menu); }

    @Nonnull
    @Override
    public Object createClientTab(@Nonnull Object screen) { return new TeamMemberEditClientTab(screen,this); }
    @Nonnull
    @Override
    protected AccessLevel accessLevel() { return AccessLevel.MEMBERS; }

    public void AddMember(@Nonnull String memberName)
    {
        if(this.isClient())
            this.menu.SendMessage(this.builder().setString("AddMember",memberName));
        else if(this.menu.selectedTeam() instanceof Team team)
            team.changeAddMember(this.menu.player,memberName);
    }

    public void AddAdmin(@Nonnull String memberName)
    {
        if(this.isClient())
            this.menu.SendMessage(this.builder().setString("AddAdmin",memberName));
        else if(this.menu.selectedTeam() instanceof Team team)
            team.changeAddAdmin(this.menu.player,memberName);
    }

    public void RemoveMember(@Nonnull String memberName)
    {
        if(this.isClient())
            this.menu.SendMessage(this.builder().setString("RemoveMember",memberName));
        else if(this.menu.selectedTeam() instanceof Team team)
            team.changeRemoveMember(this.menu.player,memberName);
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("AddMember"))
            this.AddMember(message.getString("AddMember"));
        if(message.contains("AddAdmin"))
            this.AddAdmin(message.getString("AddAdmin"));
        if(message.contains("RemoveMember"))
            this.RemoveMember(message.getString("RemoveMember"));
    }

}
