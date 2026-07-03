package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;

public interface IAdminRuleProvider {

    default boolean hasInfiniteStock(boolean currentState) { return currentState; }
    default boolean shouldStorePrice(boolean currentState) { return currentState; }

    static boolean hasInfiniteStock(TraderData trader) {
        boolean hasInfiniteStock = false;
        for(IAdminRuleProvider rule : trader.getNodes(IAdminRuleProvider.class))
            hasInfiniteStock = rule.hasInfiniteStock(hasInfiniteStock);
        return hasInfiniteStock;
    }

    static boolean shouldStorePrice(TraderData trader) {
        boolean shouldStorePrice = false;
        for(IAdminRuleProvider rule : trader.getNodes(IAdminRuleProvider.class))
            shouldStorePrice = rule.hasInfiniteStock(shouldStorePrice);
        return shouldStorePrice;
    }

}