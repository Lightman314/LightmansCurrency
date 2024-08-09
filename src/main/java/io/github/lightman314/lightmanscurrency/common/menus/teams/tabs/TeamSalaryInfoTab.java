package io.github.lightman314.lightmanscurrency.common.menus.teams.tabs;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.client.gui.screen.team.TeamSalaryInfoClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TeamManagementMenu;
import io.github.lightman314.lightmanscurrency.common.menus.teams.TeamManagementTab;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

public class TeamSalaryInfoTab extends TeamManagementTab.BankManagement {

    public TeamSalaryInfoTab(@Nonnull TeamManagementMenu menu) { super(menu); }

    private boolean isEnabled = false;

    @Override
    protected boolean canAccess(@Nonnull Player player, @Nonnull ITeam team) {
        this.isEnabled = team.isAutoSalaryEnabled();
        return super.canAccess(player,team);
    }

    @Nonnull
    @Override
    protected AccessLevel accessLevel() { return this.isEnabled ? AccessLevel.MEMBERS : AccessLevel.ADMINS; }

    @Nonnull
    @Override
    public Object createClientTab(@Nonnull Object screen) { return new TeamSalaryInfoClientTab(screen,this); }

    @Override
    public void receiveMessage(LazyPacketData message) { }

}