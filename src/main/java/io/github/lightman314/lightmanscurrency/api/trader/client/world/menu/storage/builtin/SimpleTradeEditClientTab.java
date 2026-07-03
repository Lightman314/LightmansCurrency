package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed.ClientMenuTab;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.SimpleTradeEditTab;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import net.minecraft.network.chat.Component;

public class SimpleTradeEditClientTab extends TraderStorageClientTab<SimpleTradeEditTab> {

    public static ClientMenuTab.TabBuilder<TraderStorageMenu,SimpleTradeEditTab,TraderStorageTab,TraderStorageScreen> BUILDER = SimpleTradeEditClientTab::new;

    private SimpleTradeEditClientTab(TraderStorageMenu menu,SimpleTradeEditTab commonTab,TraderStorageScreen screen) {
        super(menu, commonTab, screen);
    }

    @Override
    public IconData getIcon() { return ItemIcon.of(LCItems.TRADING_CORE); }
    @Override
    public Component getName() { return LCText.Trader.TOOLTIP_TRADE_EDIT_TAB.get(); }

    @Override
    protected void initialize(ScreenArea area,FancyPacketMap message) {
        //TODO add trade display
    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {

    }

}