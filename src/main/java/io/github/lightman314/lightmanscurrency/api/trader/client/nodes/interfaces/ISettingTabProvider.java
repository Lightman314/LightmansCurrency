package io.github.lightman314.lightmanscurrency.api.trader.client.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.SettingsTabBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionAccess;

import java.util.function.BiConsumer;

public interface ISettingTabProvider {

    void addSettingsTab(INodeAccess trader,IPermissionAccess perms,BiConsumer<TraderNodeType<?>,FancyPacketMap> sender,SettingsTabBuilder builder);

    static void collectSettings(INodeAccess trader, IPermissionAccess perms,BiConsumer<TraderNodeType<?>,FancyPacketMap> sender,SettingsTabBuilder builder) {
        for(ISettingTabProvider provider : ClientTraderNode.getClientNodes(trader, ISettingTabProvider.class))
            provider.addSettingsTab(trader,perms,sender,builder);
    }

}
