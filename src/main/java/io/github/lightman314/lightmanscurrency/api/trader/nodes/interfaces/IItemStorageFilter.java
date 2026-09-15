package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * Interface that can be applied to {@link io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNode Trader Nodes}, {@link io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData Trades}, or {@link io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice Trade Prices}
 * upon which they can determine if the given item is allowed to be placed within the traders item storage.
 */
public interface IItemStorageFilter {

    boolean itemAllowedInStorage(ItemResource resource);

    static boolean itemAllowedInStorage(INodeAccess trader,ItemResource resource) {
        //Check nodes that determine what is allowed
        for(IItemStorageFilter node : trader.getNodes(IItemStorageFilter.class))
        {
            if(node.itemAllowedInStorage(resource))
                return true;
        }
        //Next check trades
        for(TradingNode<?> tradingNode : trader.getTradingNodes())
        {
            for(TradeData trade : tradingNode.getTrades())
            {
                if(trade instanceof IItemStorageFilter filter && filter.itemAllowedInStorage(resource))
                    return true;
                if(trade.getInternalPrice() instanceof IItemStorageFilter filter && filter.itemAllowedInStorage(resource))
                    return true;
            }
        }
        return false;
    }

    default boolean denyItemExtraction(ItemResource resource) { return false; }
    static boolean denyItemExtraction(INodeAccess trader, ItemResource resource) {
        //Check nodes that determine what is allowed
        for(IItemStorageFilter node : trader.getNodes(IItemStorageFilter.class)) {
            if(!node.denyItemExtraction(resource))
                return true;
        }
        //Next check trades
        for(TradingNode<?> tradingNode : trader.getTradingNodes()) {
            for(TradeData trade : tradingNode.getTrades()) {
                if(trade instanceof IItemStorageFilter filter && !filter.denyItemExtraction(resource))
                    return true;
                if(trade.getInternalPrice() instanceof IItemStorageFilter filter && !filter.denyItemExtraction(resource))
                    return true;
            }
        }
        return false;
    }

}