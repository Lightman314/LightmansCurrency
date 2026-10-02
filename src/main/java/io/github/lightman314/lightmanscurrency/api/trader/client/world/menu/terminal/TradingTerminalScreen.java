package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.terminal;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed.TabbedMenuScreen;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.WidgetPositioner;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.terminal.builtin.NetworkTraderSelectionClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.terminal.TradingTerminalMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.terminal.TradingTerminalTab;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class TradingTerminalScreen extends TabbedMenuScreen<TradingTerminalMenu,TradingTerminalTab,TradingTerminalClientTab<?>> {

    public static final Identifier BACKGROUND = LCApi.id("container/trading_terminal/background");
    public static final Identifier BUTTON_BACKGROUND = LCApi.id("container/trading_terminal/button_background");

    public TradingTerminalScreen(TradingTerminalMenu menu,Inventory inventory) { super(menu,inventory); }

    @Override
    protected void initialize(ScreenArea area) {
        area = this.calculateSize();
        super.initialize(area);
    }

    @Override
    protected void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        //Draw the background
        gui.blitSprite(BACKGROUND,area);
        super.extractBackground(gui, area);
    }

    private ScreenArea calculateSize() {
        if(this.minecraft == null)
            return this.getArea();
        Pair<Integer,Integer> temp = NetworkTraderSelectionClientTab.calculateSize(this.minecraft);
        return this.changeSize(temp.getFirst(),temp.getSecond());
    }

    @Override
    protected IWidgetPositioner createTabPositioner() { return WidgetPositioner.leftEdge(this,20); }

}
