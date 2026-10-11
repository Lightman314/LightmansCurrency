package io.github.lightman314.lightmanscurrency.api.trader;

import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IAdminSettingProvider;
import org.apache.commons.lang3.mutable.MutableBoolean;

public final class TraderAdminSettings {
    private TraderAdminSettings() {}
    private final MutableBoolean hasInfiniteStock = new MutableBoolean(false);
    public boolean hasInfiniteStock() { return this.hasInfiniteStock.booleanValue(); }
    private final MutableBoolean shouldStorePrice = new MutableBoolean(true);
    public boolean shouldStorePrice() { return this.shouldStorePrice.booleanValue(); }
    private void update(IAdminSettingProvider provider) {
        provider.modifyInfiniteStock(this.hasInfiniteStock);
        provider.modifyStorePrice(this.shouldStorePrice);
    }

    public static TraderAdminSettings collect(INodeAccess trader) {
        TraderAdminSettings settings = new TraderAdminSettings();
        for(IAdminSettingProvider provider : trader.getNodes(IAdminSettingProvider.class)) {
            settings.update(provider);
        }
        return settings;
    }

}