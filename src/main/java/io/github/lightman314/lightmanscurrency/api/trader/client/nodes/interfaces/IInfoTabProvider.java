package io.github.lightman314.lightmanscurrency.api.trader.client.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.info.InfoTabBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;

public interface IInfoTabProvider {

    void addInfoTab(INodeAccess trader,InfoTabBuilder builder);

    static void collectInfoTab(INodeAccess trader,InfoTabBuilder builder) {
        for(IInfoTabProvider provider : ClientTraderNode.getClientNodes(trader,IInfoTabProvider.class))
            provider.addInfoTab(trader,builder);
    }

}
