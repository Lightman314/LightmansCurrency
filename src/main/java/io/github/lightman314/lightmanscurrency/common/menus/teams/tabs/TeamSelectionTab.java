package io.github.lightman314.lightmanscurrency.common.menus.teams.tabs;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.api.teams.TeamAPI;
import io.github.lightman314.lightmanscurrency.client.gui.screen.team.TeamSelectionClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TeamManagementMenu;
import io.github.lightman314.lightmanscurrency.common.menus.teams.TeamManagementTab;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

public class TeamSelectionTab extends TeamManagementTab {

    public TeamSelectionTab(@Nonnull TeamManagementMenu menu) { super(menu); }

    @Nonnull
    @Override
    public Object createClientTab(@Nonnull Object screen) { return new TeamSelectionClientTab(screen,this); }

    @Override
    public boolean canOpen(Player player) { return true; }

    public void SelectTeam(long teamID)
    {
        this.menu.SelectTeam(teamID);
        if(this.isClient())
            this.menu.SendMessage(this.builder().setLong("SelectTeam",teamID));
    }

    public void CreateTeam(@Nonnull String teamName)
    {
        if(this.isClient())
            this.menu.SendMessage(this.builder().setString("CreateTeam",teamName));
        else
        {
            ITeam team = TeamAPI.createTeam(this.menu.player,teamName);
            if(team != null)
                this.SelectTeam(team.getID());
        }
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("SelectTeam"))
            this.SelectTeam(message.getLong("SelectTeam"));
        if(message.contains("CreateTeam"))
            this.CreateTeam(message.getString("CreateTeam"));
    }

}