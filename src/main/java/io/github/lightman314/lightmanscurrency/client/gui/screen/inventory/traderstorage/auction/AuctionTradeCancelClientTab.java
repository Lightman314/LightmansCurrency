package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.auction;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.auction.AuctionTradeCancelTab;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

public class AuctionTradeCancelClientTab extends TraderStorageClientTab<AuctionTradeCancelTab> {

	public AuctionTradeCancelClientTab(TraderStorageScreen screen, AuctionTradeCancelTab commonTab) { super(screen,commonTab); }

	@Nonnull
    @Override
	public IconData getIcon() { return IconData.BLANK; }

	@Override
	public ITextComponent getTooltip() { return EasyText.empty(); }

	@Override
	public boolean tabButtonVisible() { return false; }

	@Override
	public boolean blockInventoryClosing() { return false; }

	TradeButton tradeDisplay;
	
	Button buttonCancelPlayerGive;
	Button buttonCancelStorageGive;
	
	@Override
	public void onOpen() {
		
		this.tradeDisplay = this.screen.addRenderableTabWidget(new TradeButton(this.menu::getContext, this.commonTab::getTrade, b -> {}));
		this.tradeDisplay.move(this.screen.getGuiLeft() + (this.screen.getXSize() / 2) - 47, this.screen.getGuiTop() + 17);
		
		this.buttonCancelPlayerGive = this.screen.addRenderableTabWidget(new Button(this.screen.getGuiLeft() + 40, this.screen.getGuiTop() + 60, this.screen.getXSize() - 80, 20, EasyText.translatable("button.lightmanscurrency.auction.cancel.self"), b -> this.commonTab.cancelAuction(true)));
		this.buttonCancelStorageGive = this.screen.addRenderableTabWidget(new Button(this.screen.getGuiLeft() + 40, this.screen.getGuiTop() + 85, this.screen.getXSize() - 80, 20, EasyText.translatable("button.lightmanscurrency.auction.cancel.storage"), b -> this.commonTab.cancelAuction(false)));
		
	}

	@Override
	public void renderBG(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		
		TextRenderUtil.drawCenteredText(pose, EasyText.translatable("tooltip.lightmanscurrency.auction.cancel"), this.screen.getGuiLeft() + (this.screen.getXSize() / 2), this.screen.getGuiTop() + 50, 0x404040);
		
	}

	@Override
	public void renderTooltips(MatrixStack pose, int mouseX, int mouseY) {
		
		this.tradeDisplay.renderTooltips(pose, mouseX, mouseY);
		
		if(this.buttonCancelPlayerGive.isMouseOver(mouseX, mouseY))
			this.screen.renderTooltip(pose, this.font.split(EasyText.translatable("tooltip.lightmanscurrency.auction.cancel.self"), 160), mouseX, mouseY);
		if(this.buttonCancelStorageGive.isMouseOver(mouseX, mouseY))
			this.screen.renderTooltip(pose, this.font.split(EasyText.translatable("tooltip.lightmanscurrency.auction.cancel.storage"), 160), mouseX, mouseY);
	}
	
	@Override
	public void tick() {
		//Reopen the default tab if the trade is null, or we're not allowed to edit it. (Or it's already been handled).
		AuctionTradeData trade = this.commonTab.getTrade();
		if(trade == null || !trade.isOwner(this.menu.player) || !trade.isValid())
			this.screen.changeTab(TraderStorageTab.TAB_TRADE_BASIC);
	}
	
	@Override
	public void receiveSelfMessage(CompoundNBT message) {
		if(message.contains("TradeIndex"))
			this.commonTab.setTradeIndex(message.getInt("TradeIndex"));
	}
	
	@Override
	public void receiveServerMessage(CompoundNBT message) {
		if(message.contains("CancelSuccess"))
			this.screen.changeTab(TraderStorageTab.TAB_TRADE_BASIC);
	}
	
}
