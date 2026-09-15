package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.customer.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.TradeDisplayArea;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.customer.TraderCustomerClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.customer.TraderCustomerScreen;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeIndexes;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.AbstractTabbedCustomerMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.TraderCustomerTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.builtin.NormalCustomerTab;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import net.minecraft.network.chat.Component;
public class NormalCustomerClientTab extends TraderCustomerClientTab<NormalCustomerTab> {

    public static final TabBuilder<AbstractTabbedCustomerMenu,NormalCustomerTab,TraderCustomerTab,TraderCustomerScreen> BUILDER = NormalCustomerClientTab::new;

    protected NormalCustomerClientTab(AbstractTabbedCustomerMenu menu,NormalCustomerTab commonTab,TraderCustomerScreen screen) {
        super(menu,commonTab,screen);
    }

    @Override
    public IconData getIcon() { return ItemIcon.of(LCItems.TRADING_CORE); }
    @Override
    public Component getName() { return NormalCustomerTab.TOOLTIP_CUSTOMER_TAB.get(); }

    @Override
    protected void initialize(ScreenArea area, FancyPacketMap message) {
        this.addChild(TradeDisplayArea.builder()
                .atPos(area.pos.offset(11,6))
                .ofSize(area.width - 22,111)
                .forTrader(this.getMenu().getTraderSource())
                .withContext(this.getMenu()::getContext)
                .withFilter(this::showTrade)
                .onPress(this::attemptTrade)
                .build());
    }

    protected boolean showTrade(TradeData trade) { return trade.isCustomerReady(); }

    private void attemptTrade(TraderData trader,TradingNode<?> node,TradeData trade) {
        //Lookup and
        TradeIndexes.lookup(this.getTraderSource(),trader,node,trade)
                .ifPresent(l -> this.getMenu().attemptTrade(l));
    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        gui.blitSlots(this.getCommonTab().getMoneySlots());
        gui.blitSlot(this.getCommonTab().getInteractionSlot());
    }

}