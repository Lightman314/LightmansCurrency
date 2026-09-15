package io.github.lightman314.lightmanscurrency.api.trader.trade.resources;

@FunctionalInterface
public interface ResourceCollector {

    <T> void addResource(ResourceType<T,?> type,T resource);

}