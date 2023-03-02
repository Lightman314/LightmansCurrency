package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.TraderInterfaceTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.base.TradeSelectTab;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class TradeSelectClientTab extends TraderInterfaceClientTab<TradeSelectTab> {

	public TradeSelectClientTab(TraderInterfaceScreen screen, TradeSelectTab commonTab) { super(screen, commonTab); }

	@Override
	public IconData getIcon() { return IconData.of(ModItems.TRADING_CORE); }

	@Override
	public MutableComponent getTooltip() { return new TranslatableComponent("tooltip.lightmanscurrency.interface.trade"); }

	@Override
	public boolean blockInventoryClosing() { return false; }

	@Override
	public boolean tabButtonVisible() { return this.commonTab.canOpen(this.menu.player); }
	
	TradeButtonArea tradeDisplay;
	
	@Override
	public void onOpen() {
		
		this.tradeDisplay = this.screen.addRenderableTabWidget(new TradeButtonArea(this.menu.getBE()::getTrader, trader -> this.menu.getBE().getTradeContext(), this.screen.getGuiLeft() + 3, this.screen.getGuiTop() + 17, this.screen.getXSize() - 6, 100, this.screen::addRenderableTabWidget, this.screen::removeRenderableTabWidget, this::SelectTrade, TradeButtonArea.FILTER_VALID));
		this.tradeDisplay.init();
		this.tradeDisplay.setSelectionDefinition(this::isTradeSelected);
		
	}

	@Override
	public void renderBG(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.tradeDisplay.tick();
		
		this.tradeDisplay.renderTraderName(pose, this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, this.screen.getXSize() - 16, true);
		
		this.tradeDisplay.getScrollBar().beforeWidgetRender(mouseY);
		
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
	
	private boolean isTradeSelected(TraderData trader, TradeData trade) {
		return this.menu.getBE().getTrueTrade() == trade;
	}
	
	private int getTradeIndex(TraderData trader, TradeData trade) {
		List<? extends TradeData> trades = trader.getTradeData();
		if(trades != null)
			return trades.indexOf(trade);
		return -1;
	}
	
	private void SelectTrade(TraderData trader, TradeData trade) {
		
		this.commonTab.setTradeIndex(this.getTradeIndex(trader, trade));
		
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.tradeDisplay.getScrollBar().onMouseClicked(mouseX, mouseY, button);
		return false;
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		this.tradeDisplay.getScrollBar().onMouseReleased(mouseX, mouseY, button);
		return false;
	}

}