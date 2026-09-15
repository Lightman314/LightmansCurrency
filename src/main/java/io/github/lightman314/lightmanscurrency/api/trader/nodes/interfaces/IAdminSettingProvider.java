package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;

public interface IAdminSettingProvider {

    default boolean hasInfiniteStock(boolean currentState) { return currentState; }
    default boolean shouldStorePrice(boolean currentState) { return currentState; }

    static boolean hasInfiniteStock(INodeAccess trader) {
        boolean hasInfiniteStock = false;
        for(IAdminSettingProvider rule : trader.getNodes(IAdminSettingProvider.class))
            hasInfiniteStock = rule.hasInfiniteStock(hasInfiniteStock);
        return hasInfiniteStock;
    }

    static boolean shouldStorePrice(INodeAccess trader) {
        boolean shouldStorePrice = true;
        for(IAdminSettingProvider rule : trader.getNodes(IAdminSettingProvider.class))
            shouldStorePrice = rule.shouldStorePrice(shouldStorePrice);
        return shouldStorePrice;
    }

}