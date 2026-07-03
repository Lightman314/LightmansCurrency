package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.ResourceCollector;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.ResourceType;

public interface ITradeResourceProvider {

    boolean providesResource(ResourceType<?,?> type);
    void attachResource(ResourceCollector collector);

}