package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.common;

import java.util.List;

import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.TraderClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.traders.ITraderSource;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageExecuteTrade;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.world.inventory.Slot;

import javax.annotation.Nonnull;

public class TraderInteractionTab extends TraderClientTab {

	public TraderInteractionTab(TraderScreen screen) { super(screen); }

	TradeButtonArea tradeDisplay;
	
	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {
		//Trade Button Display
		this.tradeDisplay = this.addChild(new TradeButtonArea(this.menu.traderSource, this.menu::getContext, screenArea.x + 3, screenArea.y + 17, screenArea.width - 6, 100, this::OnButtonPress, TradeButtonArea.FILTER_VALID));
		this.tradeDisplay.withTitle(screenArea.pos.offset(8,6), screenArea.width - 16, true);
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {

		TradeButton hoveredButton = this.tradeDisplay.getHoveredButton(gui.mousePos);
		if(hoveredButton != null)
		{
			//Reset texture/color
			gui.resetColor();
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
					gui.blit(TraderScreen.GUI_TEXTURE,slot.x - 1, slot.y - 1, this.screen.getXSize(), 24, 18, 18);
				}
			}
		}

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
