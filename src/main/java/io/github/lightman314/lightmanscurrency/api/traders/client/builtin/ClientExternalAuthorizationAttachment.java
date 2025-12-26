package io.github.lightman314.lightmanscurrency.api.traders.client.builtin;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.attachments.builtin.ExternalAuthorizationAttachment;
import io.github.lightman314.lightmanscurrency.api.traders.client.ClientTraderAttachment;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.BooleanPermission;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.PermissionOption;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.attachment.ExternalAuthorizationTab;

import java.util.List;

public class ClientExternalAuthorizationAttachment extends ClientTraderAttachment {

    public static final ClientTraderAttachment INSTANCE = new ClientExternalAuthorizationAttachment();
    private ClientExternalAuthorizationAttachment() {}

    @Override
    public void addSettingsTabs(TraderData trader, TraderSettingsClientTab tab, List<SettingsSubTab> tabs) {
        tabs.add(new ExternalAuthorizationTab(tab));
    }

    @Override
    public void addPermissionOptions(TraderData trader, List<PermissionOption> options) {
        options.add(BooleanPermission.of(ExternalAuthorizationAttachment.EDIT_AUTHORIZATION_PERMISSION));
    }
}
