package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.builtin;

import io.github.lightman314.lightmanscurrency.api.traders.client.IClientPermissionProvider;
import io.github.lightman314.lightmanscurrency.api.traders.client.IClientSettingsTabProvider;
import io.github.lightman314.lightmanscurrency.api.traders.client.PermissionOptionsBuilder;
import io.github.lightman314.lightmanscurrency.api.traders.client.SettingsTabBuilder;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.MachineAccessNode;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.builtin.ExternalAuthorizationTab;

public class ClientMachineAccessNode extends ClientTraderNode<MachineAccessNode> implements IClientSettingsTabProvider, IClientPermissionProvider {

    public ClientMachineAccessNode(MachineAccessNode node) { super(node); }

    @Override
    public void addSettingsTabs(TraderData trader, TraderSettingsClientTab tab, SettingsTabBuilder builder) {
        builder.add(new ExternalAuthorizationTab(tab));
    }

    @Override
    public void addPermissionOptions(TraderData trader, PermissionOptionsBuilder builder) {
        builder.addSimple(MachineAccessNode.EDIT_AUTHORIZATION_PERMISSION);
    }

}
