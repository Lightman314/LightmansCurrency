package io.github.lightman314.lightmanscurrency.features.trader.gacha;

import io.github.lightman314.lightmanscurrency.api.trader.data.templates.PersistentTraderType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.NodeCollector;
import io.github.lightman314.lightmanscurrency.features.trader.gacha.nodes.GachaStorageNode;

public class GachaTraderType extends PersistentTraderType {

    @Override
    protected void addAdditionalNodes(NodeCollector collector) {
        collector.addNode(GachaStorageNode.TYPE);
    }

}