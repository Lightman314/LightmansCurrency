package io.github.lightman314.lightmanscurrency.api.trader.data.templates;

import io.github.lightman314.lightmanscurrency.api.trader.nodes.NodeCollector;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.NetworkNode;

public abstract class NetworkTraderType extends NormalTraderType {

    @Override
    public void addNodes(NodeCollector collector) {
        collector.addNode(NetworkNode.TYPE);
        super.addNodes(collector);
    }

}