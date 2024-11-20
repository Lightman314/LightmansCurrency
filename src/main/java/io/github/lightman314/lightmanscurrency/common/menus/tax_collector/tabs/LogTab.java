package io.github.lightman314.lightmanscurrency.common.menus.tax_collector.tabs;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.tax_collector.LogClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TaxCollectorMenu;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.TaxCollectorTab;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;

import javax.annotation.Nonnull;

public class LogTab extends TaxCollectorTab {

    public LogTab(TaxCollectorMenu menu) { super(menu); }

    @Nonnull
    @Override
    public Object createClientTab(@Nonnull Object screen) { return new LogClientTab(screen, this); }

    @Override
    public void receiveMessage(LazyPacketData message) { }

}