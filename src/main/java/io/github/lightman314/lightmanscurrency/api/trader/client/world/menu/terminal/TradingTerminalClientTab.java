package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.terminal;

import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed.ClientMenuTab;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.terminal.TradingTerminalMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.terminal.TradingTerminalTab;

import java.util.List;

public abstract class TradingTerminalClientTab<T extends TradingTerminalTab> extends ClientMenuTab<TradingTerminalMenu,T,TradingTerminalTab,TradingTerminalScreen> {

    protected TradingTerminalClientTab(TradingTerminalMenu menu,T commonTab,TradingTerminalScreen screen) { super(menu,commonTab,screen); }

    public final List<TraderData> getNetworkTraders() { return this.getMenu().getTraders(); }

}