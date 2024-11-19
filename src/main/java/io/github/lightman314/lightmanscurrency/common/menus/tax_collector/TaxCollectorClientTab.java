package io.github.lightman314.lightmanscurrency.common.menus.tax_collector;

import io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed.EasyMenuClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TaxCollectorScreen;
import io.github.lightman314.lightmanscurrency.common.menus.TaxCollectorMenu;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;

public abstract class TaxCollectorClientTab<T extends TaxCollectorTab> extends EasyMenuClientTab<T,TaxCollectorMenu,TaxCollectorTab,TaxCollectorScreen,TaxCollectorClientTab<T>> {

    public final TaxEntry getEntry() { return this.menu.getEntry(); }

    protected TaxCollectorClientTab(Object screen, T commonTab) { super(screen,commonTab); }

}
