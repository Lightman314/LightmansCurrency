package io.github.lightman314.lightmanscurrency.api.trader.client.nodes.builtin;

import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.interfaces.ISettingTabProvider;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.SettingsTabBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.builtin.AlliesListTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.builtin.AllyPermissionsTab;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionAccess;

import java.util.function.BiConsumer;

public class ClientAlliesNode extends ClientTraderNode implements ISettingTabProvider {

    public static final ClientAlliesNode INSTANCE = new ClientAlliesNode();

    private ClientAlliesNode() { }

    @Override
    public void addSettingsTab(INodeAccess trader,IPermissionAccess perms,BiConsumer<TraderNodeType<?>,FancyPacketMap> sender, SettingsTabBuilder builder) {
        builder.addTab(-901,AlliesListTab::new);
        builder.addTab(-900,AllyPermissionsTab::new);
    }

}