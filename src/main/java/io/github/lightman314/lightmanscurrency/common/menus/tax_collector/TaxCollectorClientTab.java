package io.github.lightman314.lightmanscurrency.common.menus.tax_collector;

import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TaxCollectorScreen;
import io.github.lightman314.lightmanscurrency.common.menus.TaxCollectorMenu;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;

public abstract class TaxCollectorClientTab<T extends TaxCollectorTab> extends EasyTab {

    public final TaxCollectorScreen screen;
    public final TaxCollectorMenu menu;
    public final T commonTab;
    public final TaxEntry getEntry() { return this.menu.getEntry(); }

    protected TaxCollectorClientTab(Object screen, T commonTab) { super((TaxCollectorScreen)screen); this.screen = (TaxCollectorScreen)screen; this.menu = this.screen.getMenu(); this.commonTab = commonTab; }

}
