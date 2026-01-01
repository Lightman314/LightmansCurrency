package io.github.lightman314.lightmanscurrency.api.traders.client.builtin;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.client.ClientTraderAttachment;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.BooleanPermission;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.PermissionOption;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.input.InputTab;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;

import java.util.List;

public class ClientInputTraderAttachment extends ClientTraderAttachment {

    public static ClientTraderAttachment INSTANCE = new ClientInputTraderAttachment();
    private ClientInputTraderAttachment() {}

    @Override
    public void addSettingsTabs(TraderData trader, TraderSettingsClientTab tab, List<SettingsSubTab> tabs) {
        tabs.add(new InputTab(tab));
    }

    @Override
    public void addPermissionOptions(TraderData trader, List<PermissionOption> options) {
        options.add(BooleanPermission.of(Permissions.InputTrader.EXTERNAL_INPUTS));
    }
}