package io.github.lightman314.lightmanscurrency.features.trader.item;

import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.features.trader.item.nodes.ItemTradesNode;

public class ItemTraderType extends AbstractItemTraderType {

    @Override
    protected TraderNodeType<ItemTradesNode> getTradeNode() { return ItemTradesNode.TYPE; }

}