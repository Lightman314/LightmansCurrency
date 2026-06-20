package io.github.lightman314.lightmanscurrency.api.trader.trade.resources;

public interface ResourceCollector {

    <T> void addResource(ResourceType<T,?> type,T resource);

}