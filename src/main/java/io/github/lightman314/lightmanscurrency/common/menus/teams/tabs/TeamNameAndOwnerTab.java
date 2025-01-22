package io.github.lightman314.lightmanscurrency.common.menus.teams.tabs;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.client.gui.screen.team.TeamNameAndOwnerClientTab;
import io.github.lightman314.lightmanscurrency.common.data.types.TeamDataCache;
import io.github.lightman314.lightmanscurrency.common.menus.TeamManagementMenu;
import io.github.lightman314.lightmanscurrency.common.menus.teams.TeamManagementTab;
import io.github.lightman314.lightmanscurrency.common.teams.Team;

import javax.annotation.Nonnull;

public class TeamNameAndOwnerTab extends TeamManagementTab.Management {

    public TeamNameAndOwnerTab(@Nonnull TeamManagementMenu menu) { super(menu); }

    @Nonnull
    @Override
    public Object createClientTab(@Nonnull Object screen) { return new TeamNameAndOwnerClientTab(screen,this); }

    @Nonnull
    @Override
    protected AccessLevel accessLevel() { return AccessLevel.ADMINS; }

    public void ChangeName(@Nonnull String newName)
    {
        ITeam team = this.menu.selectedTeam();
        if(team instanceof Team t)
            t.changeName(this.menu.player,newName);
        if(this.isClient())
            this.menu.SendMessage(this.builder().setString("ChangeName",newName));

    }

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
            {
                TeamDataCache data = TeamDataCache.TYPE.get(false);
                if(data == null)
                    return;
                data.removeTeam(team.getID());
            }
        }
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("ChangeName"))
            this.ChangeName(message.getString("ChangeName"));
        if(message.contains("SetOwner"))
            this.SetOwner(message.getString("SetOwner"));
        if(message.contains("DisbandTeam"))
            this.DisbandTeam();
    }

}
