package io.github.lightman314.lightmanscurrency.client.features.player_data;

import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed.ClientMenuTab;
import io.github.lightman314.lightmanscurrency.features.player_data.PlayerDataMenu;
import io.github.lightman314.lightmanscurrency.features.player_data.PlayerDataTab;

public abstract class PlayerDataClientTab<T extends PlayerDataTab> extends ClientMenuTab<PlayerDataMenu,T,PlayerDataTab,PlayerDataScreen> {

    protected PlayerDataClientTab(PlayerDataMenu menu, T commonTab, PlayerDataScreen screen) {
        super(menu, commonTab, screen);
    }

}