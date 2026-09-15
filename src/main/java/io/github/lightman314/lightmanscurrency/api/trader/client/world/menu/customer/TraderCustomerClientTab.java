package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.customer;

import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed.ClientMenuTab;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderSource;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.AbstractTabbedCustomerMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.TraderCustomerAccess;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.TraderCustomerTab;

public abstract class TraderCustomerClientTab<T extends TraderCustomerTab> extends ClientMenuTab<AbstractTabbedCustomerMenu,T, TraderCustomerTab, TraderCustomerScreen> implements TraderCustomerAccess {

    @Override
    public final TraderSource getTraderSource() { return this.getMenu().getTraderSource(); }

    protected TraderCustomerClientTab(AbstractTabbedCustomerMenu menu, T commonTab, TraderCustomerScreen screen) {
        super(menu, commonTab, screen);
    }

    public boolean showMoneyInfo() { return true; }


}