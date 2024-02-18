package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.auction;

import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.TraderClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.network.message.auction.CPacketSubmitBid;
import net.minecraft.ChatFormatting;

import javax.annotation.Nonnull;

public class AuctionBidTab extends TraderClientTab {

	private final long auctionHouseID;
	private final int tradeIndex;
	
	private AuctionHouseTrader getAuctionHouse() {
		TraderData data = TraderSaveData.GetTrader(true, this.auctionHouseID);
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
		
		this.tradeDisplay = this.addChild(new TradeButton(() -> this.menu.getContext(this.getAuctionHouse()), this::getTrade, b -> {}));
		this.tradeDisplay.setPosition(screenArea.pos.offset(screenArea.width / 2 - this.tradeDisplay.getWidth() / 2, 5));
		
		this.bidAmount = this.addChild(new MoneyValueWidget(screenArea.pos.offset(screenArea.width / 2 - MoneyValueWidget.WIDTH / 2, 10 + this.tradeDisplay.getHeight()), firstOpen ? null : this.bidAmount, this.getTrade().getMinNextBid(), MoneyValueWidget.EMPTY_CONSUMER));
		this.bidAmount.allowFreeInput = false;
		this.bidAmount.drawBG = false;
		
		this.bidButton = this.addChild(new EasyTextButton(screenArea.pos.offset(22, 119), 68, 20, EasyText.translatable("gui.lightmanscurrency.auction.bid"), this::SubmitBid));
		
		this.closeButton = this.addChild(new EasyTextButton(screenArea.pos.offset(screenArea.width - 25, 5), 20, 20, EasyText.literal("X").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD), this::close));
		
		this.tick();
		
	}
	
	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) { }
	
	@Override
	public void tick() {
		if(this.getTrade() == null)
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
