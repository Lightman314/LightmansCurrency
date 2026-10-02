package io.github.lightman314.lightmanscurrency.features.trader.item;

import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.features.trader.item.nodes.ArmorTradesNode;

public class ArmorTraderType extends AbstractItemTraderType {

    @Override
    protected TraderNodeType<ArmorTradesNode> getTradeNode() { return ArmorTradesNode.TYPE; }

}