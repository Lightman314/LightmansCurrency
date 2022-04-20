package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.ITradeData;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.menus.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.menus.traderinterface.TraderInterfaceTab;
import io.github.lightman314.lightmanscurrency.menus.traderinterface.base.TradeSelectTab;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class TradeSelectClientTab extends TraderInterfaceClientTab<TradeSelectTab> {

	public TradeSelectClientTab(TraderInterfaceScreen screen, TradeSelectTab commonTab) { super(screen, commonTab); }

	@Override
	public IconData getIcon() { return IconData.of(ModItems.TRADING_CORE); }

	@Override
	public Component getTooltip() { return new TranslatableComponent("tooltip.lightmanscurrency.interface.trade"); }

	@Override
	public boolean blockInventoryClosing() { return false; }

	@Override
	public boolean tabButtonVisible() { return this.commonTab.canOpen(this.menu.player); }
	
	TradeButtonArea tradeDisplay;
	
	@Override
	public void onOpen() {
		
		this.tradeDisplay = this.screen.addRenderableTabWidget(new TradeButtonArea(this.menu.getBE()::getTrader, trader -> this.menu.getBE().getTradeContext(), this.screen.getGuiLeft() + 3, this.screen.getGuiTop() + 17, this.screen.getXSize() - 6, 100, 2, this.screen::addRenderableTabWidget, this.screen::removeRenderableTabWidget, this::SelectTrade, TradeButtonArea.FILTER_VALID));		
		this.tradeDisplay.init();
		this.tradeDisplay.setSelectionDefinition(this::isTradeSelected);
		
	}

	@Override
	public void renderBG(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.tradeDisplay.tick();
		
		this.tradeDisplay.renderTraderName(pose, this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, this.screen.getXSize() - 16, true);
		
	}

	@Override
	public void renderTooltips(PoseStack pose, int mouseX, int mouseY) {
		
		this.tradeDisplay.renderTooltips(this.screen, pose, 0, 0, 0, mouseX, mouseY);
		
	}
	
	@Override
	public void tick() {
		if(!this.commonTab.canOpen(this.menu.player))
		{
			this.screen.changeTab(TraderInterfaceTab.TAB_INFO);
			return;
		}
	}
	
	private boolean isTradeSelected(ITrader trader, ITradeData trade) {
		return this.menu.getBE().getTrueTrade() == trade;
	}
	
	private int getTradeIndex(ITrader trader, ITradeData trade) {
		List<? extends ITradeData> trades = trader.getTradeInfo();
		if(trades != null)
			return trades.indexOf(trade);
		return -1;
	}
	
	private void SelectTrade(ITrader trader, ITradeData trade) {
		
		this.commonTab.setTradeIndex(this.getTradeIndex(trader, trade));
		
	}

}
