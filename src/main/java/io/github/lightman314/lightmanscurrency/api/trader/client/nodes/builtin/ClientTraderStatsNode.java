package io.github.lightman314.lightmanscurrency.api.trader.client.nodes.builtin;

import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.interfaces.IInfoTabProvider;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.info.InfoTabBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.info.builtin.TraderStatsTab;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;

public class ClientTraderStatsNode extends ClientTraderNode implements IInfoTabProvider {

    public static final ClientTraderStatsNode INSTANCE = new ClientTraderStatsNode();

    private ClientTraderStatsNode() {}

    @Override
    public void addInfoTab(INodeAccess trader,InfoTabBuilder builder) {
        builder.addTab(TraderStatsTab::new);
    }

}
