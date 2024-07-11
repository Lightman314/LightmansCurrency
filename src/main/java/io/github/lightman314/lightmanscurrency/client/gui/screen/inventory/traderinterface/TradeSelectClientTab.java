package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.base.TradeSelectTab;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public class TradeSelectClientTab extends TraderInterfaceClientTab<TradeSelectTab> {

	public TradeSelectClientTab(TraderInterfaceScreen screen, TradeSelectTab commonTab) { super(screen, commonTab); }

	@Nonnull
	@Override
	public IconData getIcon() { return IconData.of(ModItems.TRADING_CORE); }

	@Override
	public MutableComponent getTooltip() { return LCText.TOOLTIP_INTERFACE_TRADE_SELECT.get(); }

	@Override
	public boolean tabButtonVisible() { return this.commonTab.canOpen(this.menu.player); }

	@Override
	public boolean blockInventoryClosing() { return this.tradeDisplay.isSearchBoxRelevant(); }

	TradeButtonArea tradeDisplay;
	
	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {
		
		this.tradeDisplay = this.addChild(new TradeButtonArea(this.menu.getBE()::getTrader, trader -> this.menu.getBE().getTradeContext(), this.screen.getGuiLeft() + 3, this.screen.getGuiTop() + 17, this.screen.getXSize() - 6, 100, this::SelectTrade, TradeButtonArea.FILTER_VALID)
				.withTitle(this.screen.getCorner().offset(4,6), this.screen.getXSize() - 8, false));
		this.tradeDisplay.setSelectionDefinition(this::isTradeSelected);
		
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) { }
	
	@Override
	public void tick() {
		if(!this.commonTab.canOpen(this.menu.player))
			this.screen.changeTab(TraderInterfaceTab.TAB_INFO);
	}
	
	private boolean isTradeSelected(TraderData trader, TradeData trade) {
		return this.menu.getBE().getTrueTrade() == trade;
	}
	
	private int getTradeIndex(TraderData trader, TradeData trade) {
		return trader.getTradeData().indexOf(trade);
	}
	
	private void SelectTrade(TraderData trader, TradeData trade) {
		
		this.commonTab.setTradeIndex(this.getTradeIndex(trader, trade));
		
	}

}
