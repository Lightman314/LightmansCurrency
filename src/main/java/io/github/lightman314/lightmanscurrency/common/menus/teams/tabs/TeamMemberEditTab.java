package io.github.lightman314.lightmanscurrency.common.menus.teams.tabs;

import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
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

    public void PromotePlayer(@Nonnull PlayerReference player)
    {
        if(this.menu.selectedTeam() instanceof Team team)
            team.changePromoteMember(this.menu.player,player);
        if(this.isClient())
            this.menu.SendMessage(this.builder().setCompound("PromotePlayer",player.save()));
    }

    public void DemotePlayer(@Nonnull PlayerReference player)
    {
        if(this.menu.selectedTeam() instanceof Team team)
            team.changeDemoteMember(this.menu.player,player);
        if(this.isClient())
            this.menu.SendMessage(this.builder().setCompound("DemotePlayer",player.save()));
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("PromotePlayer"))
            this.PromotePlayer(PlayerReference.load(message.getNBT("PromotePlayer")));
        if(message.contains("DemotePlayer"))
            this.DemotePlayer(PlayerReference.load(message.getNBT("DemotePlayer")));
    }

}