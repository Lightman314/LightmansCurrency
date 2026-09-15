package io.github.lightman314.lightmanscurrency.api.trader.event;

import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderSource;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.AbstractTabbedCustomerMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.TraderCustomerTab;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.TabBuilder;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

import java.util.List;
import java.util.function.Function;

/**
 * @see Pre
 * @see Post
 */
public sealed class CustomerMenuTabsEvent extends Event permits CustomerMenuTabsEvent.Pre, CustomerMenuTabsEvent.Post {

    public final Player getPlayer() { return this.menu.getPlayer(); }
    public final TraderSource getTraderSource() { return this.menu.getTraderSource(); }
    public final List<TraderData> getTraders() { return this.menu.getTraders(); }

    private final TabBuilder<AbstractTabbedCustomerMenu, TraderCustomerTab> builder;
    public TabBuilder<AbstractTabbedCustomerMenu, TraderCustomerTab> getBuilder() { return this.builder; }
    private final AbstractTabbedCustomerMenu menu;
    public AbstractTabbedCustomerMenu getMenu() { return this.menu; }

    public CustomerMenuTabsEvent(TabBuilder<AbstractTabbedCustomerMenu, TraderCustomerTab> builder, AbstractTabbedCustomerMenu menu) {
        this.builder = builder;
        this.menu = menu;
    }

    public final void safeAddTab(Function<AbstractTabbedCustomerMenu, TraderCustomerTab> factory) { this.safeAddTab(factory.apply(this.menu)); }

    public final void safeAddTab(TraderCustomerTab tab) { this.builder.safeAddTab(tab); }

    public static final class Pre extends CustomerMenuTabsEvent {
        public Pre(TabBuilder<AbstractTabbedCustomerMenu, TraderCustomerTab> builder, AbstractTabbedCustomerMenu menu) { super(builder, menu); }
    }

    public static final class Post extends CustomerMenuTabsEvent {
        public Post(TabBuilder<AbstractTabbedCustomerMenu, TraderCustomerTab> builder, AbstractTabbedCustomerMenu menu) { super(builder, menu); }
    }


}