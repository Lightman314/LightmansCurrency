package io.github.lightman314.lightmanscurrency.common.menus.tax_collector.tabs;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.tax_collector.LogClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TaxCollectorMenu;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.TaxCollectorTab;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;

public class LogTab extends TaxCollectorTab {

    public LogTab(TaxCollectorMenu menu) { super(menu); }

    @Override
    public Object createClientTab(Object screen) { return new LogClientTab(screen, this); }

    @Override
    public void onTabOpen() { }

    @Override
    public void onTabClose() { }

    @Override
    public void receiveMessage(LazyPacketData message) { }

}