package io.github.lightman314.lightmanscurrency.api.trader.data.templates;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.NodeCollector;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.PersistentDataNode;

public abstract class PersistentTraderType extends NetworkTraderType {

    @Override
    @OverridingMethodsMustInvokeSuper
    public void addNodes(NodeCollector collector) {
        collector.addNode(PersistentDataNode.TYPE);
        super.addNodes(collector);
    }
}