package io.github.lightman314.lightmanscurrency.api.trader.data.templates;

import io.github.lightman314.lightmanscurrency.api.trader.nodes.NodeCollector;

public abstract class PersistentTraderType extends NetworkTraderType {

    @Override
    public void addNodes(NodeCollector collector) {
        //TODO add persistent node
        super.addNodes(collector);
    }
}