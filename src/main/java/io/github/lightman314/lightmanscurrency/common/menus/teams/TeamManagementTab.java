package io.github.lightman314.lightmanscurrency.common.menus.teams;

import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.common.menus.TeamManagementMenu;
import io.github.lightman314.lightmanscurrency.common.menus.tabbed.EasyMenuTab;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

public abstract class TeamManagementTab extends EasyMenuTab<TeamManagementMenu,TeamManagementTab> {

    public TeamManagementTab(@Nonnull TeamManagementMenu menu) { super(menu); }

    @Override
    public void onTabOpen() {}

    @Override
    public void onTabClose() { }

    public static abstract class Management extends TeamManagementTab
    {
        public Management(@Nonnull TeamManagementMenu menu) { super(menu); }
        @Override
        public final boolean canOpen(Player player) {
            ITeam team = this.menu.selectedTeam();
            return team != null && this.canAccess(player,team);
        }
        protected enum AccessLevel {
            OWNER,
            ADMINS,
            MEMBERS
        }
        @Nonnull
        protected abstract AccessLevel accessLevel();
        //Not final just in case we make a tab that only appears if a certain setting is enabled for the team
        protected boolean canAccess(@Nonnull Player player, @Nonnull ITeam team)
        {
            return switch (this.accessLevel()) {
                case OWNER -> team.isOwner(player);
                case ADMINS -> team.isAdmin(player);
                case MEMBERS -> team.isMember(player);
            };
        }
    }

    public static abstract class BankManagement extends Management
    {
        public BankManagement(@Nonnull TeamManagementMenu menu) { super(menu); }
        @Override
        protected boolean canAccess(@Nonnull Player player, @Nonnull ITeam team) { return super.canAccess(player, team) && team.hasBankAccount(); }
    }

}
