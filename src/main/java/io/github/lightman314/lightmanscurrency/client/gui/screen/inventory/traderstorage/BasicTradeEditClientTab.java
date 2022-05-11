package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea.InteractionConsumer;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.ITradeData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.trades_basic.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class BasicTradeEditClientTab<T extends BasicTradeEditTab> extends TraderStorageClientTab<T> implements InteractionConsumer{

	public BasicTradeEditClientTab(TraderStorageScreen screen, T commonTab) { super(screen, commonTab); this.commonTab.setClientHandler(screen);}

	@Override
	public IconData getIcon() { return IconAndButtonUtil.ICON_TRADELIST; }

	@Override
	public Component getTooltip() { return new TranslatableComponent("tooltip.lightmanscurrency.trader.edit_trades"); }

	@Override
	public boolean tabButtonVisible() { return true; }
	
	@Override
	public boolean blockInventoryClosing() { return false; }

	TradeButtonArea tradeDisplay;
	
	@Override
	public void onOpen() {
		
		this.tradeDisplay = this.screen.addRenderableTabWidget(new TradeButtonArea(this.menu.traderSource, trader -> this.menu.getContext(), this.screen.getGuiLeft() + 3, this.screen.getGuiTop() + 17, this.screen.getXSize() - 6, 100, 2, this.screen::addRenderableTabWidget, this.screen::removeRenderableTabWidget, (t1,t2) -> {}, TradeButtonArea.FILTER_ANY));		
		this.tradeDisplay.init();
		this.tradeDisplay.setInteractionConsumer(this);
		
	}

	@Override
	public void renderBG(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.tradeDisplay.tick();
		
		this.tradeDisplay.renderTraderName(pose, this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, this.screen.getXSize() - 16, true);
		
		this.tradeDisplay.getScrollBar().beforeWidgetRender(mouseY);
		
	}

	@Override
	public void renderTooltips(PoseStack pose, int mouseX, int mouseY) {
		
		if(this.menu.getCarried().isEmpty())
			this.tradeDisplay.renderTooltips(this.screen, pose, this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, this.screen.getXSize() - 16, mouseX, mouseY);
		
	}

	@Override
	public void onTradeButtonInputInteraction(ITrader trader, ITradeData trade, int index, int mouseButton) {
		trade.onInputDisplayInteraction(this.commonTab, this.screen, index, mouseButton, this.menu.getCarried());
	}

	@Override
	public void onTradeButtonOutputInteraction(ITrader trader, ITradeData trade, int index, int mouseButton) {
		trade.onOutputDisplayInteraction(this.commonTab, this.screen, index, mouseButton, this.menu.getCarried());
	}
	
	@Override
	public void onTradeButtonInteraction(ITrader trader, ITradeData trade, int localMouseX, int localMouseY, int mouseButton) {
		trade.onInteraction(this.commonTab, this.screen, localMouseX, localMouseY, mouseButton, this.menu.getCarried());
	}

}
