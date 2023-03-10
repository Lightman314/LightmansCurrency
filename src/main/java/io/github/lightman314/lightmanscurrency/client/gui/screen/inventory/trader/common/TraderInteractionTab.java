package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.common;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.TraderClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.util.RenderUtil;
import io.github.lightman314.lightmanscurrency.common.traders.ITraderSource;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageExecuteTrade;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.inventory.container.Slot;

public class TraderInteractionTab extends TraderClientTab {

	public TraderInteractionTab(TraderScreen screen) { super(screen); }

	@Override
	public boolean blockInventoryClosing() { return false; }

	TradeButtonArea tradeDisplay;

	@Override
	public void onOpen() {
		//Trade Button Display
		this.tradeDisplay = this.screen.addRenderableTabWidget(new TradeButtonArea(this.menu.traderSource, this.menu::getContext, this.screen.getGuiLeft() + 3, this.screen.getGuiTop() + 17, this.screen.getXSize() - 6, 100, this.screen::addRenderableTabWidget, this.screen::removeRenderableTabWidget, this::OnButtonPress, TradeButtonArea.FILTER_VALID));
		this.tradeDisplay.init();
	}

	@Override
	public void renderBG(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		this.tradeDisplay.renderTraderName(pose, this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, this.screen.getXSize() - 16, false);
		this.tradeDisplay.getScrollBar().beforeWidgetRender(mouseY);

		TradeButton hoveredButton = this.tradeDisplay.getHoveredButton(mouseX, mouseY);
		if(hoveredButton != null)
		{
			//Reset texture/color
			RenderUtil.bindTexture(TraderScreen.GUI_TEXTURE);
			RenderUtil.color4f(1f, 1f, 1f, 1f);
			//Get highlighted slot info from the trade
			TradeData trade = hoveredButton.getTrade();
			TradeContext context = hoveredButton.getContext();
			List<Integer> relevantSlots = trade.getRelevantInventorySlots(context, this.menu.slots);
			for(int s : relevantSlots)
			{
				if(s >= 0 && s < this.menu.slots.size())
				{
					Slot slot = this.menu.slots.get(s);
					//Replace slot bg with the hightlighted version.
					this.screen.blit(pose, this.screen.getGuiLeft() + slot.x - 1, this.screen.getGuiTop() + slot.y - 1, this.screen.getXSize(), 24, 18, 18);
				}
			}
		}

	}

	@Override
	public void renderTooltips(MatrixStack pose, int mouseX, int mouseY) {
		if(this.menu.player.inventory.getCarried().isEmpty())
			this.tradeDisplay.renderTooltips(this.screen, pose, this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, this.screen.getXSize() - 16, mouseX, mouseY);
	}

	@Override
	public void tick() { this.tradeDisplay.tick(); }

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

	private static long lastPress = 0;

	private void OnButtonPress(TraderData trader, TradeData trade) {

		if(trader == null || trade == null)
			return;

		//Force 10ms between trades because for some odd reason the Trade Buttons are triggering twice...
		if(TimeUtil.compareTime(10, lastPress))
			return;
		lastPress = TimeUtil.getCurrentTime();

		ITraderSource ts = this.menu.traderSource.get();
		if(ts == null)
		{
			this.menu.player.closeContainer();
			return;
		}

		List<TraderData> traders = ts.getTraders();
		int ti = traders.indexOf(trader);
		if(ti < 0)
			return;

		TraderData t = traders.get(ti);
		if(t == null)
			return;

		int tradeIndex = t.getTradeData().indexOf(trade);
		if(tradeIndex < 0)
			return;

		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageExecuteTrade(ti, tradeIndex));

	}

}