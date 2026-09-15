package io.github.lightman314.lightmanscurrency.api.trader;

import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IAdminSettingProvider;

public final class TraderAdminSettings {
    private TraderAdminSettings() {}
    private boolean hasInfiniteStock = false;
    public boolean hasInfiniteStock() { return this.hasInfiniteStock; }
    private boolean shouldStorePrice = true;
    public boolean shouldStorePrice() { return this.shouldStorePrice; }
    private void update(IAdminSettingProvider provider) {
        this.hasInfiniteStock = provider.hasInfiniteStock(this.hasInfiniteStock);
        this.shouldStorePrice = provider.shouldStorePrice(this.shouldStorePrice);
    }

    public static TraderAdminSettings collect(INodeAccess trader) {
        TraderAdminSettings settings = new TraderAdminSettings();
        for(IAdminSettingProvider provider : trader.getNodes(IAdminSettingProvider.class)) {
            settings.update(provider);
        }
        return settings;
    }

}