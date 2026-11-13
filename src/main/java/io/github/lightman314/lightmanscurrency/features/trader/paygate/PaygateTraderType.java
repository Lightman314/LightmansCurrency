package io.github.lightman314.lightmanscurrency.features.trader.paygate;

import io.github.lightman314.lightmanscurrency.api.trader.data.templates.NormalTraderType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.NodeCollector;
import io.github.lightman314.lightmanscurrency.features.trader.paygate.nodes.PaygateTradesNode;

public class PaygateTraderType extends NormalTraderType {

    @Override
    protected void addAdditionalNodes(NodeCollector collector) {
        collector.addNode(PaygateTradesNode.TYPE,16);
    }

}
