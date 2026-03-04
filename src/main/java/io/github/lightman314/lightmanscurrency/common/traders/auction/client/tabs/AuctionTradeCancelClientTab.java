package io.github.lightman314.lightmanscurrency.common.traders.auction.client.tabs;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.auction.trade.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tabs.AuctionTradeCancelTab;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.util.TooltipHelper;
import net.minecraft.network.chat.Component;

public class AuctionTradeCancelClientTab extends TraderStorageClientTab<AuctionTradeCancelTab> {

	public AuctionTradeCancelClientTab(Object screen, AuctionTradeCancelTab commonTab) { super(screen,commonTab); }

	@Override
	public IconData getIcon() { return IconData.Null(); }

	@Override
	public Component getTooltip() { return EasyText.empty(); }

	@Override
	public boolean tabVisible() { return false; }

	TradeButton tradeDisplay;
	
	EasyButton buttonCancelPlayerGive;
	EasyButton buttonCancelStorageGive;
	
	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {
		
		this.tradeDisplay = this.addChild(TradeButton.builder()
				.position(screenArea.pos.offset((screenArea.width / 2) - 47,17))
				.context(this.menu::getContext)
				.trade(this.commonTab::getTrade)
				.build());
		
		this.buttonCancelPlayerGive = this.addChild(EasyTextButton.builder()
				.position(screenArea.pos.offset(40,60))
				.width(screenArea.width - 80)
				.text(LCText.BUTTON_TRADER_AUCTION_CANCEL_SELF)
				.pressAction(() -> this.commonTab.cancelAuction(true))
				.addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRADER_AUCTION_CANCEL_SELF,TooltipHelper.DEFAULT_TOOLTIP_WIDTH))
				.build());
		this.buttonCancelStorageGive = this.addChild(EasyTextButton.builder()
				.position(screenArea.pos.offset(40,85))
				.width(screenArea.width - 80)
				.text(LCText.BUTTON_TRADER_AUCTION_CANCEL_STORAGE.get())
				.pressAction(() -> this.commonTab.cancelAuction(false))
				.addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRADER_AUCTION_CANCEL_STORAGE,TooltipHelper.DEFAULT_TOOLTIP_WIDTH))
				.build());
		
	}

	@Override
	public void renderBG(EasyGuiGraphics gui) {
		
		TextRenderUtil.drawCenteredText(gui, LCText.GUI_TRADER_AUCTION_CANCEL.get(), (this.screen.getXSize() / 2), 50, 0x404040);
		
	}
	
	@Override
	public void tick() {
		//Reopen the default tab if the trade is null, or we're not allowed to edit it. (Or it's already been handled).
		AuctionTradeData trade = this.commonTab.getTrade();
		if(trade == null || !trade.isOwner(this.screen.getPlayer()) || !trade.isValid())
			this.screen.ChangeTab(BasicTradeEditTab.KEY);
	}
	
	@Override
	public void receiveServerMessage(LazyPacketData message) {
		if(message.contains("CancelSuccess"))
			this.screen.ChangeTab(BasicTradeEditTab.KEY);
	}
	
}
