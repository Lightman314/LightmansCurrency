package io.github.lightman314.lightmanscurrency.client.features.atm.tabs;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed.ClientMenuTab;
import io.github.lightman314.lightmanscurrency.api.coins.atm.client.ExchangePageManager;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.client.features.atm.ATMClientTab;
import io.github.lightman314.lightmanscurrency.client.features.atm.ATMScreen;
import io.github.lightman314.lightmanscurrency.core.LCBlocks;
import io.github.lightman314.lightmanscurrency.features.atm.ATMMenu;
import io.github.lightman314.lightmanscurrency.features.atm.ATMTab;
import io.github.lightman314.lightmanscurrency.features.atm.tabs.CoinExchangeTab;
import net.minecraft.network.chat.Component;

public class CoinExchangeClientTab extends ATMClientTab<CoinExchangeTab> {

    public static final ClientMenuTab.TabBuilder<ATMMenu,CoinExchangeTab,ATMTab,ATMScreen> BUILDER = CoinExchangeClientTab::new;

    private final ExchangePageManager exchangeManager;

    protected CoinExchangeClientTab(ATMMenu menu, CoinExchangeTab commonTab, ATMScreen screen) {
        super(menu, commonTab, screen);
        this.exchangeManager = new ExchangePageManager(this.getPlayer(),this,this.getCommonTab()::runATMCommand);
    }

    @Override
    public IconData getIcon() { return ItemIcon.of(LCBlocks.ATM); }
    @Override
    public Component getName() { return CoinExchangeTab.TOOLTIP.get(); }

    @Override
    protected void initialize(ScreenArea area, FancyPacketMap message) {
        this.exchangeManager.initialize(area);
    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        //Render title
        gui.text(LCBlocks.ATM.get().getName(),8,6,0xFF404040,false);
    }

}
