package io.github.lightman314.lightmanscurrency.common.menus;

import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.api.teams.TeamAPI;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.menus.providers.EasyMenuProvider;
import io.github.lightman314.lightmanscurrency.common.menus.tabbed.EasyTabbedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.teams.TeamManagementTab;
import io.github.lightman314.lightmanscurrency.common.menus.teams.tabs.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TeamManagementMenu extends EasyTabbedMenu<TeamManagementMenu,TeamManagementTab> {

    public static final MenuProvider PROVIDER = new Provider();

    private long selectedTeam = -1;
    public boolean hasTeam() { return this.selectedTeam >= 0 && this.selectedTeam() != null; }
    @Nullable
    public ITeam selectedTeam() { return TeamAPI.API.GetTeam(this, this.selectedTeam); }
    public void SelectTeam(long team) { this.selectedTeam = team; }

    public TeamManagementMenu(int windowID, @Nonnull Inventory inventory) { super(ModMenus.TEAM_MANAGEMENT.get(), windowID, inventory); this.initializeTabs(); }

    @Override
    protected void registerTabs() {
        this.addTab(new TeamSelectionTab(this));
        this.addTab(new TeamMemberEditTab(this));
        this.addTab(new TeamBankAccountTab(this));
        this.addTab(new TeamSalaryInfoTab(this));
        this.addTab(new TeamSalarySettingsTab(this));
        this.addTab(new TeamSalaryPaymentsTab(this));
        this.addTab(new TeamStatsTab(this));
        this.addTab(new TeamNameAndOwnerTab(this));
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int slot) { return ItemStack.EMPTY; }

    private static class Provider implements EasyMenuProvider {
        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int windowID, @Nonnull Inventory inventory, @Nonnull Player player) { return new TeamManagementMenu(windowID,inventory); }
    }

}
