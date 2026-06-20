package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.ResourceCollector;

public interface ITradeResourceProvider {

    void attachResource(ResourceCollector collector);

}