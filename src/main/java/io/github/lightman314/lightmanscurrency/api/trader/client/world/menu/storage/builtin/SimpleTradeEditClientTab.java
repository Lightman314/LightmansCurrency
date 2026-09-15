package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed.ClientMenuTab;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.TradeDisplayArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderSource;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.SimpleTradeEditTab;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import net.minecraft.network.chat.Component;

public class SimpleTradeEditClientTab extends TraderStorageClientTab<SimpleTradeEditTab> {

    public static final ClientMenuTab.TabBuilder<TraderStorageMenu,SimpleTradeEditTab,TraderStorageTab,TraderStorageScreen> BUILDER = SimpleTradeEditClientTab::new;

    private SimpleTradeEditClientTab(TraderStorageMenu menu,SimpleTradeEditTab commonTab,TraderStorageScreen screen) {
        super(menu, commonTab, screen);
    }

    @Override
    public IconData getIcon() { return ItemIcon.of(LCItems.TRADING_CORE); }
    @Override
    public Component getName() { return SimpleTradeEditTab.TOOLTIP_TRADE_EDIT_TAB.get(); }

    @Override
    protected void initialize(ScreenArea area,FancyPacketMap message) {
        this.addChild(TradeDisplayArea.builder()
                .atPos(area.pos.offset(11,6))
                .ofSize(area.width - 22,111)
                .forTrader(TraderSource.deferred(this.getMenu()::getTrader))
                .withContext(t -> this.getMenu().getTradeContext())
                .withFilter(this::showTrade)
                .withInteractionHandler(this.getCommonTab())
                .build());
    }

    protected boolean showTrade(TradeData trade) { return true; }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) { }

}