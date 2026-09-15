package io.github.lightman314.lightmanscurrency.features.trader.item;

import io.github.lightman314.lightmanscurrency.api.trader.nodes.NodeCollector;
import io.github.lightman314.lightmanscurrency.features.trader.item.nodes.ArmorTradesNode;
import io.github.lightman314.lightmanscurrency.features.trader.item.nodes.ItemTradesNode;

public class ArmorTraderType extends ItemTraderType {

    @Override
    protected void addAdditionalNodes(NodeCollector collector) {
        super.addAdditionalNodes(collector);
        //Replace the item trades node with the armor trades node
        collector.replaceNode(ItemTradesNode.TYPE,ArmorTradesNode.TYPE);
    }
}