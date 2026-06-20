package io.github.lightman314.lightmanscurrency.features.trader.item;

import io.github.lightman314.lightmanscurrency.api.trader.nodes.NodeCollector;
import io.github.lightman314.lightmanscurrency.api.trader.data.templates.PersistentTraderType;
import io.github.lightman314.lightmanscurrency.features.trader.item.nodes.ItemTradesNode;
import io.github.lightman314.lightmanscurrency.features.trader.misc.ItemStorageNode;

public class ItemTraderType extends PersistentTraderType {

    public static final ItemTraderType INSTANCE = new ItemTraderType();

    @Override
    protected void addAdditionalNodes(NodeCollector collector) {
        collector.addNode(ItemStorageNode.TYPE);
        collector.addNode(ItemTradesNode.TYPE);
    }

}