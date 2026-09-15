package io.github.lightman314.lightmanscurrency.api.trader.client.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.info.InfoClientSubTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.info.InfoClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;

import java.util.function.Consumer;

public interface IInfoTabProvider {

    void addInfoTab(INodeAccess trader,InfoClientTab tab,Consumer<InfoClientSubTab> builder);

    static void collectSettings(INodeAccess trader,InfoClientTab tab,Consumer<InfoClientSubTab> builder) {
        for(IInfoTabProvider provider : ClientTraderNode.getClientNodes(trader,IInfoTabProvider.class))
            provider.addInfoTab(trader,tab,builder);
    }

}
