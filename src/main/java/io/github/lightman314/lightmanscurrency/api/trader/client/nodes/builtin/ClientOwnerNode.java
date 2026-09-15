package io.github.lightman314.lightmanscurrency.api.trader.client.nodes.builtin;

import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.interfaces.ISettingTabProvider;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.SettingsTabBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.builtin.OwnerSettingsTab;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionAccess;

import java.util.function.BiConsumer;

public class ClientOwnerNode extends ClientTraderNode implements ISettingTabProvider {

    public static final ClientOwnerNode INSTANCE = new ClientOwnerNode();

    protected ClientOwnerNode() {}

    @Override
    public void addSettingsTab(INodeAccess trader, IPermissionAccess perms, BiConsumer<TraderNodeType<?>, FancyPacketMap> sender, SettingsTabBuilder builder) {
        builder.addTab(1000,OwnerSettingsTab::new);
    }

}