package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.auction;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.TraderClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.auction.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.auction.MessageSubmitBid;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

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

	@Override
	public boolean blockInventoryClosing() { return false; }
	
	//Auction Bid Display
	TradeButton tradeDisplay;
	
	//Bid Amount Input
	CoinValueInput bidAmount;
	
	//Bid Button
	Button bidButton;
	
	Button closeButton;
	
	@Override
	public void onOpen() {
		
		if(this.getTrade() == null)
			return;
		
		this.tradeDisplay = this.screen.addRenderableTabWidget(new TradeButton(() -> this.menu.getContext(this.getAuctionHouse()), this::getTrade, b -> {}));
		this.tradeDisplay.move(this.screen.getGuiLeft() + this.screen.getXSize() / 2 - this.tradeDisplay.getWidth() / 2, this.screen.getGuiTop() + 5);
		
		this.bidAmount = this.screen.addRenderableTabWidget(new CoinValueInput(this.screen.getGuiLeft() + this.screen.getXSize() / 2 - CoinValueInput.DISPLAY_WIDTH / 2, this.screen.getGuiTop() + 10 + this.tradeDisplay.getHeight(), new TranslatableComponent("gui.lightmanscurrency.auction.bidamount"), this.getTrade().getMinNextBid(), this.font, v -> {}, this.screen::addRenderableTabWidget));
		this.bidAmount.init();
		this.bidAmount.allowFreeToggle = false;
		this.bidAmount.drawBG = false;
		
		this.bidButton = this.screen.addRenderableTabWidget(new Button(this.screen.getGuiLeft() + 22, this.screen.getGuiTop() + 119, 68, 20, new TranslatableComponent("gui.lightmanscurrency.auction.bid"), this::SubmitBid));
		
		this.closeButton = this.screen.addRenderableTabWidget(new Button(this.screen.getGuiLeft() + this.screen.getXSize() - 25, this.screen.getGuiTop() + 5, 20, 20, new TextComponent("X").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD), this::close));
		
		this.tick();
		
	}
	
	@Override
	public void renderBG(PoseStack pose, int mouseX, int mouseY, float partialTicks) { }

	@Override
	public void renderTooltips(PoseStack pose, int mouseX, int mouseY) {
		
		this.tradeDisplay.renderTooltips(pose, mouseX, mouseY);
		
	}
	
	@Override
	public void tick() {
		if(this.getTrade() == null)
		{
			this.screen.closeTab();
			return;
		}
		
		if(this.bidAmount != null)
		{
			long bidQuery = this.bidAmount.getCoinValue().getRawValue();
			CoinValue minBid = this.getTrade().getMinNextBid();
			if(bidQuery < minBid.getRawValue())
				this.bidAmount.setCoinValue(this.getTrade().getMinNextBid());
			this.bidButton.active = this.menu.getContext(this.getAuctionHouse()).getAvailableFunds() >= bidQuery;
			
			this.bidAmount.tick();
		}
		
	}
	
	private void SubmitBid(Button button) {
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSubmitBid(this.auctionHouseID, this.tradeIndex, this.bidAmount.getCoinValue()));
		this.screen.closeTab();
	}
	
	private void close(Button button) { this.screen.closeTab(); }
	
}