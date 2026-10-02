package io.github.lightman314.lightmanscurrency.api.trader.world.menu.terminal.builtin;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.INetworkController;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.AbstractTabbedCustomerMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.terminal.TradingTerminalMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.terminal.TradingTerminalTab;
import net.minecraft.resources.Identifier;

public class NetworkTraderSelectionTab extends TradingTerminalTab {

    public static final Identifier KEY = LCApi.id("network_trader_selection");

    public static final TextEntry NAME = TextEntry.tooltip(LCApi.MODID,"trader.terminal.network_trader_selection");
    public static final TextEntry TOOLTIP_OPEN_ALL_TRADERS = TextEntry.tooltip(LCApi.MODID,"network_terminal.open_all");

    public NetworkTraderSelectionTab(TradingTerminalMenu menu) { super(menu); }

    @Override
    public Identifier getClientTabKey() { return KEY; }

    public boolean hasAnyNetworkTraders() { return !this.getNetworkTraders().isEmpty(); }

    public void openAllTraders() {
        if(this.isClient()) {
            this.send(FancyPacketMap.flag("openAllTraders"));
            return;
        }
        if(this.hasAnyNetworkTraders())
            this.getPlayer().openMenu(AbstractTabbedCustomerMenu.allNetworkTraderProvider(this.getMenu().getValidator(),false));
    }

    public void openTrader(long traderID) {
        if(this.isClient()) {
            this.send(FancyPacketMap.map().setLong("openTrader",traderID));
            return;
        }
        TraderData trader = LCApi.getTraderAPI().getTrader(this,traderID);
        if(trader != null && INetworkController.visibleToNetwork(trader))
            trader.openCustomerMenu(this.getPlayer(),this.getMenu().getValidator(),false);
    }

    @Override
    public void handleMessage(FancyPacketMap message) {
        if(message.contains("openAllTraders"))
            this.openAllTraders();
        if(message.contains("openTrader"))
            this.openTrader(message.getLong("openTrader"));
    }

}
