package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea.InteractionConsumer;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.trades_basic.BasicTradeEditTab;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class BasicTradeEditClientTab<T extends BasicTradeEditTab> extends TraderStorageClientTab<T> implements InteractionConsumer{

	public BasicTradeEditClientTab(TraderStorageScreen screen, T commonTab) { super(screen, commonTab); this.commonTab.setClientHandler(screen);}

	@Override
	public IconData getIcon() { return IconAndButtonUtil.ICON_TRADELIST; }

	@Override
	public MutableComponent getTooltip() { return new TranslatableComponent("tooltip.lightmanscurrency.trader.edit_trades"); }

	@Override
	public boolean tabButtonVisible() { return true; }
	
	@Override
	public boolean blockInventoryClosing() { return false; }

	TradeButtonArea tradeDisplay;
	
	Button buttonAddTrade;
	Button buttonRemoveTrade;
	
	@Override
	public void onOpen() {
		
		this.tradeDisplay = this.screen.addRenderableTabWidget(new TradeButtonArea(this.menu.traderSource, t -> this.menu.getContext(), this.screen.getGuiLeft() + 3, this.screen.getGuiTop() + 17, this.screen.getXSize() - 6, 100, this.screen::addRenderableTabWidget, this.screen::removeRenderableTabWidget, (t1,t2) -> {}, this.menu.getTrader() == null ? TradeButtonArea.FILTER_ANY : this.menu.getTrader().getStorageDisplayFilter(this.menu)));
		this.tradeDisplay.init();
		this.tradeDisplay.setInteractionConsumer(this);
		
		this.buttonAddTrade = this.screen.addRenderableTabWidget(new PlainButton(this.screen.getGuiLeft() + this.screen.getXSize() - 25, this.screen.getGuiTop() + 4, 10, 10, this::AddTrade, TraderScreen.GUI_TEXTURE, TraderScreen.WIDTH + 18, 20));
		this.buttonRemoveTrade = this.screen.addRenderableTabWidget(new PlainButton(this.screen.getGuiLeft() + this.screen.getXSize() - 14, this.screen.getGuiTop() + 4, 10, 10, this::RemoveTrade, TraderScreen.GUI_TEXTURE, TraderScreen.WIDTH + 28, 20));
		
		this.tick();
		
	}

	@Override
	public void renderBG(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.tradeDisplay.tick();
		
		this.tradeDisplay.renderTraderName(pose, this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, this.screen.getXSize() - (this.renderAddRemoveButtons() ? 32 : 16), true);
		
		this.tradeDisplay.getScrollBar().beforeWidgetRender(mouseY);
		
	}

	private boolean renderAddRemoveButtons() {
		if(this.menu.getTrader() != null)
			return this.menu.getTrader().canEditTradeCount();
		return false;
	}
	
	@Override
	public void tick() {
		super.tick();
		
		TraderData trader = this.menu.getTrader();
		if(trader != null)
		{
			this.buttonAddTrade.visible = this.buttonRemoveTrade.visible = trader.canEditTradeCount();
			this.buttonAddTrade.active = trader.getTradeCount() < trader.getMaxTradeCount();
			this.buttonRemoveTrade.active = trader.getTradeCount() > 1;
		}
		else
			this.buttonAddTrade.visible = this.buttonRemoveTrade.visible = false;
	}
	
	@Override
	public void renderTooltips(PoseStack pose, int mouseX, int mouseY) {
		
		if(this.menu.getCarried().isEmpty())
			this.tradeDisplay.renderTooltips(this.screen, pose, this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, this.screen.getXSize() - (this.renderAddRemoveButtons() ? 27 : 16), mouseX, mouseY);
		
	}

	@Override
	public void onTradeButtonInputInteraction(TraderData trader, TradeData trade, int index, int mouseButton) {
		trade.onInputDisplayInteraction(this.commonTab, this.screen, index, mouseButton, this.menu.getCarried());
	}

	@Override
	public void onTradeButtonOutputInteraction(TraderData trader, TradeData trade, int index, int mouseButton) {
		trade.onOutputDisplayInteraction(this.commonTab, this.screen, index, mouseButton, this.menu.getCarried());
	}
	
	@Override
	public void onTradeButtonInteraction(TraderData trader, TradeData trade, int localMouseX, int localMouseY, int mouseButton) {
		trade.onInteraction(this.commonTab, this.screen, localMouseX, localMouseY, mouseButton, this.menu.getCarried());
	}
	
	private void AddTrade(Button button) { this.commonTab.addTrade(); }
	
	private void RemoveTrade(Button button) { this.commonTab.removeTrade(); }
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.tradeDisplay.getScrollBar().onMouseClicked(mouseX, mouseY, button);
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		this.tradeDisplay.getScrollBar().onMouseReleased(mouseX, mouseY, button);
		return super.mouseReleased(mouseX, mouseY, button);
	}

}