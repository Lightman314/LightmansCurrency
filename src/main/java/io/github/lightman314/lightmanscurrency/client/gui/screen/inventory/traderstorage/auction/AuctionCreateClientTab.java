package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.auction;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.menus.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.auction.AuctionCreateTab;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.persistentdata.MessageAddPersistentAuction;
import io.github.lightman314.lightmanscurrency.trader.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class AuctionCreateClientTab extends TraderStorageClientTab<AuctionCreateTab> {

	public static final long CLOSE_DELAY = TimeUtil.DURATION_SECOND * 5;
	public static final long MAX_DAY_COUNT = 30;
	
	public AuctionCreateClientTab(TraderStorageScreen screen, AuctionCreateTab commonTab) { super(screen, commonTab); }
	
	@Override
	public IconData getIcon() { return IconAndButtonUtil.ICON_PLUS; }
	
	@Override
	public Component getTooltip() { return new TranslatableComponent("tooltip.lightmanscurrency.auction.create"); }
	
	@Override
	public boolean tabButtonVisible() { return true; }
	
	@Override
	public boolean blockInventoryClosing() { return false; }
	
	AuctionTradeData pendingAuction;
	
	TradeButton tradeDisplay;
	
	CoinValueInput priceSelect;
	Button buttonTogglePriceMode;
	boolean startingBidMode = true;
	
	long auctionCreated = 0;
	
	Button buttonIncreaseHour;
	Button buttonDecreaseHour;
	Button buttonIncreaseDay;
	Button buttonDecreaseDay;
	
	private int dayCount;
	private int hourCount;
	
	Button buttonSubmitAuction;
	
	boolean locked = false;
	long successTime = 0;
	
	Button buttonSubmitPersistentAuction;
	EditBox persistentAuctionIDInput;
	
	@Override
	public void onOpen() {
		
		this.pendingAuction = new AuctionTradeData(this.menu.player);
		this.dayCount = 1;
		this.hourCount = 0;
		this.updateDuration();
		this.locked = false;
		this.successTime = 0;
		this.startingBidMode = true;
		
		this.tradeDisplay = this.screen.addRenderableTabWidget(new TradeButton(this.menu::getContext, () -> this.pendingAuction, b -> {}));
		this.tradeDisplay.move(this.screen.getGuiLeft() + 15, this.screen.getGuiTop() + 5);
		
		this.priceSelect = this.screen.addRenderableTabWidget(new CoinValueInput(this.screen.getGuiLeft() + this.screen.getXSize() / 2 - CoinValueInput.DISPLAY_WIDTH / 2, this.screen.getGuiTop() + 34, new TextComponent(""), CoinValue.EMPTY, this.font, this::onPriceChanged, this.screen::addRenderableTabWidget));
		this.priceSelect.init();
		this.priceSelect.drawBG = this.priceSelect.allowFreeToggle = false;
		
		this.buttonTogglePriceMode = this.screen.addRenderableTabWidget(new Button(this.screen.getGuiLeft() + 114, this.screen.getGuiTop() + 5, this.screen.getXSize() - 119, 20, new TranslatableComponent("button.lightmanscurrency.auction.toggleprice.startingbid"), b -> this.TogglePriceTarget()));
		
		this.commonTab.getAuctionItems().addListener(c -> this.UpdateAuctionItems());
		
		//Duration Buttons
		this.buttonIncreaseDay = this.screen.addRenderableTabWidget(new PlainButton(this.screen.getGuiLeft() + 80, this.screen.getGuiTop() + 112, 20, 10, b -> this.changeDayCount(1), CoinValueInput.GUI_TEXTURE, 0, CoinValueInput.HEIGHT));
		this.buttonDecreaseDay = this.screen.addRenderableTabWidget(new PlainButton(this.screen.getGuiLeft() + 80, this.screen.getGuiTop() + 135, 20, 10, b -> this.changeDayCount(-1), CoinValueInput.GUI_TEXTURE, 20, CoinValueInput.HEIGHT));
		
		this.buttonIncreaseHour = this.screen.addRenderableTabWidget(new PlainButton(this.screen.getGuiLeft() + 110, this.screen.getGuiTop() + 112, 20, 10, b -> this.changeHourCount(1), CoinValueInput.GUI_TEXTURE, 0, CoinValueInput.HEIGHT));
		this.buttonDecreaseHour = this.screen.addRenderableTabWidget(new PlainButton(this.screen.getGuiLeft() + 110, this.screen.getGuiTop() + 135, 20, 10, b -> this.changeHourCount(-1), CoinValueInput.GUI_TEXTURE, 20, CoinValueInput.HEIGHT));
		
		//Submit Button
		this.buttonSubmitAuction = this.screen.addRenderableTabWidget(new Button(this.screen.getGuiLeft() + 40, this.screen.getGuiTop() - 20, this.screen.getXSize() - 80, 20, new TranslatableComponent("button.lightmanscurrency.auction.create"), b -> this.submitAuction()));
		this.buttonSubmitAuction.active = false;
		
		
		this.buttonSubmitPersistentAuction = this.screen.addRenderableTabWidget(new IconButton(this.screen.getGuiLeft() + this.screen.getXSize() - 20, this.screen.getGuiTop() - 20, this::submitPersistentAuction, IconAndButtonUtil.ICON_PERSISTENT_DATA, IconAndButtonUtil.TOOLTIP_PERSISTENT_AUCTION));
		this.buttonSubmitPersistentAuction.visible = TradingOffice.isAdminPlayer(this.screen.getMenu().player);
		this.buttonSubmitPersistentAuction.active = false;

		int idWidth = this.font.width(new TranslatableComponent("gui.lightmanscurrency.settings.persistent.id"));
		this.persistentAuctionIDInput = this.screen.addRenderableTabWidget(new EditBox(this.font, this.screen.getGuiLeft() + idWidth + 2, this.screen.getGuiTop() - 40, this.screen.getXSize() - idWidth - 2, 18, new TextComponent("")));
		this.persistentAuctionIDInput.visible = TradingOffice.isAdminPlayer(this.screen.getMenu().player);
		
	}
	
	@Override
	public void onClose() {
		this.commonTab.getAuctionItems().removeListener(c -> this.UpdateAuctionItems());
	}
	
	@Override
	public void renderBG(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		RenderSystem.setShaderTexture(0, TraderScreen.GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		for(SimpleSlot slot : this.commonTab.getSlots())
		{
			//Render Slot BG's
			this.screen.blit(pose, this.screen.getGuiLeft() + slot.x - 1, this.screen.getGuiTop() + slot.y - 1, TraderScreen.WIDTH, 0, 18, 18);
		}
		
		//Item Slot label
		this.font.draw(pose, new TranslatableComponent("gui.lightmanscurrency.auction.auctionitems"), this.screen.getGuiLeft() + TraderMenu.SLOT_OFFSET + 7, this.screen.getGuiTop() + 112, 0x404040);
		
		//Duration labels
		TextRenderUtil.drawCenteredText(pose, new TranslatableComponent("gui.lightmanscurrency.auction.duration.day", this.dayCount), this.screen.getGuiLeft() + 90, this.screen.getGuiTop() + 124, 0x404040);
		TextRenderUtil.drawCenteredText(pose, new TranslatableComponent("gui.lightmanscurrency.auction.duration.hour", this.hourCount), this.screen.getGuiLeft() + 120, this.screen.getGuiTop() + 124, 0x404040);
		
		if(this.locked && this.successTime != 0)
			TextRenderUtil.drawCenteredText(pose, new TranslatableComponent("gui.lightmanscurrency.auction.create.success").withStyle(ChatFormatting.BOLD), this.screen.getGuiLeft() + this.screen.getXSize() / 2, 34, 0x404040);
		
		if(TradingOffice.isAdminPlayer(this.screen.getMenu().player))
		{
			this.font.draw(pose, new TranslatableComponent("gui.lightmanscurrency.settings.persistent.id"), this.screen.getGuiLeft(), this.screen.getGuiTop() - 35, 0xFFFFFF);
		}
		
	}
	
	@Override
	public void renderTooltips(PoseStack pose, int mouseX, int mouseY) {
		
		this.tradeDisplay.renderTooltips(pose, mouseX, mouseY);
		
		IconAndButtonUtil.renderButtonTooltips(pose, mouseX, mouseY, Lists.newArrayList(this.buttonSubmitPersistentAuction));
		
	}
	
	@Override
	public void tick() {
		this.priceSelect.locked = this.locked;
		this.priceSelect.tick();
		if(this.locked && this.successTime != 0)
		{
			if(TimeUtil.compareTime(CLOSE_DELAY, this.successTime))
			{
				this.screen.changeTab(TraderStorageTab.TAB_TRADE_BASIC);
				return;
			}
		}
		if(this.locked)
		{
			this.buttonTogglePriceMode.active = this.buttonDecreaseDay.active = this.buttonIncreaseDay.active = this.buttonDecreaseHour.active = 
			this.buttonIncreaseHour.active = this.buttonSubmitAuction.active = false;
		}
		else
		{
			this.buttonTogglePriceMode.active = true;
			if(this.dayCount >= Config.SERVER.maxAuctionDuration.get())
			{
				this.buttonIncreaseDay.active = this.buttonIncreaseHour.active = false;
			}
			else
				this.buttonIncreaseDay.active = this.buttonIncreaseHour.active = true;
			this.buttonDecreaseDay.active = this.dayCount > 0;
			this.buttonDecreaseHour.active = this.dayCount > 0 || this.hourCount > 1;
			this.buttonSubmitAuction.active = this.pendingAuction.isValid();
		}
		
		if(TradingOffice.isAdminPlayer(this.screen.getMenu().player))
		{
			this.buttonSubmitPersistentAuction.visible = this.persistentAuctionIDInput.visible = !this.locked;
			this.buttonSubmitPersistentAuction.active = this.pendingAuction.isValid();
			this.persistentAuctionIDInput.tick();
		}
		else
			this.buttonSubmitPersistentAuction.visible = this.persistentAuctionIDInput.visible = false;
		
	}
	
	private void UpdateAuctionItems() {
		this.pendingAuction.setAuctionItems(this.commonTab.getAuctionItems());
	}
	
	private void onPriceChanged(CoinValue newPrice) {
		if(this.startingBidMode)
			this.pendingAuction.setStartingBid(newPrice);
		else
			this.pendingAuction.setMinBidDifferent(newPrice);
	}
	
	private void TogglePriceTarget() {
		this.startingBidMode = !this.startingBidMode;
		this.buttonTogglePriceMode.setMessage(new TranslatableComponent(this.startingBidMode ? "button.lightmanscurrency.auction.toggleprice.startingbid" : "button.lightmanscurrency.auction.toggleprice.mindeltabid"));
		if(this.startingBidMode)
			this.priceSelect.setCoinValue(this.pendingAuction.getLastBidAmount());
		else
			this.priceSelect.setCoinValue(this.pendingAuction.getMinBidDifference());
	}
	
	private void changeHourCount(int delta) {
		this.hourCount = this.hourCount + delta;
		if(this.hourCount < 0)
		{
			if(this.dayCount > 0)
			{
				this.hourCount += 24;
				this.changeDayCount(-1);
				return;
			}
			else
				this.hourCount = 1;
		}
		if(this.hourCount >= 24)
		{
			int addToDays = this.hourCount / 24;
			this.hourCount = this.hourCount % 24;
			this.changeDayCount(addToDays);
		}
		else
			this.updateDuration();
	}
	
	private void changeDayCount(int delta) {
		this.dayCount = MathUtil.clamp(this.dayCount + delta, 0, Config.SERVER.maxAuctionDuration.get());
		if(this.dayCount < 1 && this.hourCount < 1)
			this.dayCount = 1;
		if(this.dayCount >= Config.SERVER.maxAuctionDuration.get())
			this.hourCount = 0;
		this.updateDuration();
	}
	
	private void updateDuration() {
		this.pendingAuction.setDuration(this.dayCount * TimeUtil.DURATION_DAY + this.hourCount * TimeUtil.DURATION_HOUR);
	}
	
	private void submitAuction() {
		//LightmansCurrency.LogInfo("Sending Auction to the server!\n" + this.pendingAuction.getAsNBT().getAsString());
		this.commonTab.createAuction(this.pendingAuction);
		this.locked = true;
		for(SimpleSlot slot : this.commonTab.getSlots())
			slot.locked = true;
	}
	
	private void submitPersistentAuction(Button button) {
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddPersistentAuction(this.pendingAuction.getAsNBT(), this.persistentAuctionIDInput.getValue()));
	}
	
	@Override
	public void receiveServerMessage(CompoundTag message) {
		if(message.contains("AuctionCreated"))
		{
			//LightmansCurrency.LogInfo("Received create response message from the server.\nAuction Created: " + message.getBoolean("AuctionCreated"));
			if(message.getBoolean("AuctionCreated"))
				this.successTime = TimeUtil.getCurrentTime();
			else
			{
				this.locked = false;
				for(SimpleSlot slot : this.commonTab.getSlots())
					slot.locked = false;
			}
		}
	}
	
}
