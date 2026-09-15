package io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer;

import io.github.lightman314.lightmanscurrency.api.trader.data.TraderSource;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.MenuTab;

public abstract class TraderCustomerTab extends MenuTab<AbstractTabbedCustomerMenu> implements TraderCustomerAccess{

    public static final int SLOT_OFFSET = AbstractTabbedCustomerMenu.SLOT_OFFSET;

    @Override
    public final TraderSource getTraderSource() { return this.getMenu().getTraderSource(); }

    public TraderCustomerTab(AbstractTabbedCustomerMenu menu) {
        super(menu);
    }

    public void buildContext(TradeContext.Builder builder) { }

}