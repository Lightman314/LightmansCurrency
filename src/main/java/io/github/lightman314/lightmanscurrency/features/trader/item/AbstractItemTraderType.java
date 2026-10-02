package io.github.lightman314.lightmanscurrency.features.trader.item;

import io.github.lightman314.lightmanscurrency.api.trader.data.templates.PersistentTraderType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.NodeCollector;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.ExternalInteractionsNode;
import io.github.lightman314.lightmanscurrency.features.trader.item.nodes.AbstractItemTradesNode;
import io.github.lightman314.lightmanscurrency.features.trader.item_common.ItemStorageNode;

import javax.annotation.OverridingMethodsMustInvokeSuper;

public abstract class AbstractItemTraderType extends PersistentTraderType {

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void addAdditionalNodes(NodeCollector collector) {
        collector.addNode(ItemStorageNode.TYPE);
        collector.addNode(ExternalInteractionsNode.TYPE);
        collector.addNode(this.getTradeNode());
    }

    protected abstract TraderNodeType<? extends AbstractItemTradesNode<?>> getTradeNode();

}
