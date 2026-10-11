package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import org.apache.commons.lang3.mutable.MutableBoolean;

public interface IAdminSettingProvider {

    default void modifyInfiniteStock(MutableBoolean currentState) { }
    default void modifyStorePrice(MutableBoolean currentState) {  }

    static boolean hasInfiniteStock(INodeAccess trader) {
        MutableBoolean hasInfiniteStock = new MutableBoolean(false);
        for(IAdminSettingProvider rule : trader.getNodes(IAdminSettingProvider.class))
            rule.modifyInfiniteStock(hasInfiniteStock);
        return hasInfiniteStock.booleanValue();
    }

    static boolean shouldStorePrice(INodeAccess trader) {
        MutableBoolean shouldStorePrice = new MutableBoolean(true);
        for(IAdminSettingProvider rule : trader.getNodes(IAdminSettingProvider.class))
            rule.modifyStorePrice(shouldStorePrice);
        return shouldStorePrice.booleanValue();
    }

}