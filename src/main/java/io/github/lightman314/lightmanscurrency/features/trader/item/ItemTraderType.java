package io.github.lightman314.lightmanscurrency.features.trader.item;

import io.github.lightman314.lightmanscurrency.api.trader.nodes.NodeCollector;
import io.github.lightman314.lightmanscurrency.api.trader.data.templates.PersistentTraderType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.CapabilityInteractionNode;
import io.github.lightman314.lightmanscurrency.features.trader.item.nodes.ItemTradesNode;
import io.github.lightman314.lightmanscurrency.features.trader.item_common.ItemStorageNode;

import javax.annotation.OverridingMethodsMustInvokeSuper;

public class ItemTraderType extends PersistentTraderType {

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void addAdditionalNodes(NodeCollector collector) {
        collector.addNode(ItemStorageNode.TYPE);
        collector.addNode(CapabilityInteractionNode.TYPE);
        collector.addNode(ItemTradesNode.TYPE);
    }

}