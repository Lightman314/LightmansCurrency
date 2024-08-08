package io.github.lightman314.lightmanscurrency.common.menus.teams;

import io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed.EasyMenuClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.common.menus.TeamManagementMenu;

import javax.annotation.Nonnull;

public abstract class TeamManagementClientTab<T extends TeamManagementTab> extends EasyMenuClientTab<T,TeamManagementMenu,TeamManagementTab,TeamManagerScreen,TeamManagementClientTab<T>> {

    public TeamManagementClientTab(@Nonnull Object screen, @Nonnull T commonTab) { super(screen, commonTab); }

}
