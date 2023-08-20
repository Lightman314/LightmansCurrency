package io.github.lightman314.lightmanscurrency.common.menus.tax_collector.tabs;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.tax_collector.InfoClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TaxCollectorMenu;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.TaxCollectorTab;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;

public class InfoTab extends TaxCollectorTab {

    public InfoTab(TaxCollectorMenu menu) { super(menu); }

    @Override
    public Object createClientTab(Object screen) { return new InfoClientTab(screen, this); }

    @Override
    public void onTabOpen() { }

    @Override
    public void onTabClose() { }

    public boolean CanClearCache(TaxEntry entry) { return entry != null && (entry.isServerEntry() || entry.getOwner().isAdmin(this.menu.player)); }

    public void ClearInfoCache()
    {
        TaxEntry entry = this.getEntry();
        if(this.CanClearCache(entry))
        {
            entry.stats.clear();
            if(this.isClient())
                this.menu.SendMessageToServer(LazyPacketData.simpleFlag("ClearInfoCache"));
        }
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("ClearInfoCache"))
            this.ClearInfoCache();
    }

}
