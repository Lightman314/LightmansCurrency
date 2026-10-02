package io.github.lightman314.lightmanscurrency.api.trader.world.menu.terminal;

import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.MenuTab;

import java.util.List;

public abstract class TradingTerminalTab extends MenuTab<TradingTerminalMenu> {

    public TradingTerminalTab(TradingTerminalMenu menu) { super(menu); }

    public List<TraderData> getNetworkTraders() { return this.getMenu().getTraders(); }

}
