package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.builtin;

import io.github.lightman314.lightmanscurrency.api.traders.client.IClientPermissionProvider;
import io.github.lightman314.lightmanscurrency.api.traders.client.IClientSettingsTabProvider;
import io.github.lightman314.lightmanscurrency.api.traders.client.PermissionOptionsBuilder;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.client.SettingsTabBuilder;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.builtin.OwnershipTab;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.OwnerNode;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;

public class ClientOwnerNode extends ClientTraderNode<OwnerNode> implements IClientPermissionProvider, IClientSettingsTabProvider {

    public ClientOwnerNode(OwnerNode node) { super(node); }

    @Override
    public void addPermissionOptions(TraderData trader, PermissionOptionsBuilder builder) {
        builder.addSimple(Permissions.TRANSFER_OWNERSHIP);
    }

    @Override
    public void addSettingsTabs(TraderData trader, TraderSettingsClientTab tab, SettingsTabBuilder builder) {
        builder.add(new OwnershipTab(tab),100);
    }
}
