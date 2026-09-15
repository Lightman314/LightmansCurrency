package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.AbstractTabbedCustomerMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.TraderCustomerTab;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.TabBuilder;

public interface ICustomerMenuProvider {

    void addCustomerTabs(TabBuilder<AbstractTabbedCustomerMenu, TraderCustomerTab> builder, AbstractTabbedCustomerMenu menu);

}
