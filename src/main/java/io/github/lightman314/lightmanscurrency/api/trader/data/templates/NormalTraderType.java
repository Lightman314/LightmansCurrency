package io.github.lightman314.lightmanscurrency.api.trader.data.templates;

import io.github.lightman314.lightmanscurrency.api.trader.data.TraderType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.NodeCollector;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.*;

public abstract class NormalTraderType extends TraderType {

    @Override
    public void addNodes(NodeCollector collector) {
        collector.addNode(OwnerNode.TYPE);
        collector.addNode(WorldNode.TYPE);
        collector.addNode(DisplayNode.TYPE);
        collector.addNode(AlliesNode.TYPE);
        collector.addNode(UpgradeNode.TYPE);
        this.addAdditionalNodes(collector);
    }

    protected abstract void addAdditionalNodes(NodeCollector collector);

}