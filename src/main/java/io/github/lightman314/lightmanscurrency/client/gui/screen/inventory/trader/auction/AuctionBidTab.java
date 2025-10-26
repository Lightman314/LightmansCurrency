package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.auction;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.TraderClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.network.message.auction.CPacketSubmitBid;

import javax.annotation.Nonnull;

public class AuctionBidTab extends TraderClientTab {

	private final long auctionHouseID;
	private final int tradeIndex;
	
	private AuctionHouseTrader getAuctionHouse() {
		TraderData data = TraderAPI.getApi().GetTrader(true, this.auctionHouseID);
		if(data instanceof AuctionHouseTrader)
			return (AuctionHouseTrader)data;
		return null;
	}
	
	private AuctionTradeData getTrade() {
		AuctionHouseTrader trader = this.getAuctionHouse();
		if(trader != null)
			return trader.getTrade(this.tradeIndex);
		return null;
	}
	
	public AuctionBidTab(TraderScreen screen, long auctionHouseID, int tradeIndex) { super(screen); this.auctionHouseID = auctionHouseID; this.tradeIndex = tradeIndex; }
	
	//Auction Bid Display
	TradeButton tradeDisplay;
	
	//Bid Amount Input
	MoneyValueWidget bidAmount;
	
	//Bid Button
	EasyButton bidButton;

	EasyButton closeButton;
	
	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {
		
		if(this.getTrade() == null)
			return;
		
		this.tradeDisplay = this.addChild(TradeButton.builder()
				.context(() -> this.menu.getContext(this.getAuctionHouse()))
				.trade(this::getTrade)
				.build());
		this.tradeDisplay.setPosition(screenArea.pos.offset(screenArea.width / 2 - this.tradeDisplay.getWidth() / 2, 5));
		
		this.bidAmount = this.addChild(MoneyValueWidget.builder()
				.position(screenArea.pos.offset(screenArea.width / 2 - MoneyValueWidget.WIDTH / 2,10 + this.tradeDisplay.getHeight()))
				.oldIfNotFirst(firstOpen,this.bidAmount)
				.startingValue(this.getTrade().getMinNextBid())
				.allowHandlerChange(false)
				.blockFreeInputs()
				.build());
		
		this.bidButton = this.addChild(EasyTextButton.builder()
				.position(screenArea.pos.offset(22,119))
				.width(68)
				.text(LCText.BUTTON_TRADER_AUCTION_BID)
				.pressAction(this::SubmitBid)
				.build());
		
		this.closeButton = this.addChild(IconButton.builder()
				.position(screenArea.pos.offset(screenArea.width - 25,5))
				.pressAction(this::close)
				.icon(IconUtil.ICON_X)
				.build());
		
		this.tick();
		
	}
	
	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) { }
	
	@Override
	public void tick() {
		AuctionTradeData trade = this.getTrade();
		if(trade == null || !trade.isValid() || !trade.allowedToBid(this.menu.getPlayer()))
		{
			this.screen.closeTab();
			return;
		}
		
		if(this.bidAmount != null)
		{
			MoneyValue oldBid = this.bidAmount.getCurrentValue();
			MoneyValue minBid = this.getTrade().getMinNextBid();
			if(!oldBid.containsValue(minBid))
				this.bidAmount.changeValue(this.getTrade().getMinNextBid());
			this.bidButton.active = this.menu.getContext(this.getAuctionHouse()).getAvailableFunds().containsValue(this.bidAmount.getCurrentValue());
		}
	}
	
	private void SubmitBid(EasyButton button) {
		new CPacketSubmitBid(this.auctionHouseID, this.tradeIndex, this.bidAmount.getCurrentValue()).send();
		this.screen.closeTab();
	}
	
	private void close(EasyButton button) { this.screen.closeTab(); }
}
