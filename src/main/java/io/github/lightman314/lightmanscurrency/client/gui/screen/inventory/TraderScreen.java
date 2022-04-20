package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.List;

import org.anti_ad.mc.ipn.api.IPNIgnore;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.ITradeData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageExecuteTrade;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.ITraderSource;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

@IPNIgnore
public class TraderScreen extends AbstractContainerScreen<TraderMenu>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/trader.png");
	
	TradeButtonArea tradeDisplay;
	
	public static final int WIDTH = 206;
	public static final int HEIGHT = 236;
	
	Button buttonOpenStorage;
	Button buttonCollectCoins;
	
	Button buttonOpenTerminal;
	
	public TraderScreen(TraderMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.imageWidth = WIDTH;
		this.imageHeight = HEIGHT;
	}
	
	@Override
	public void init() {
		
		super.init();
		
		this.buttonOpenStorage = this.addRenderableWidget(IconAndButtonUtil.storageButton(this.leftPos, this.topPos - 20, this::OpenStorage, () -> this.menu.isSingleTrader() && this.menu.getSingleTrader().hasPermission(this.menu.player, Permissions.OPEN_STORAGE)));
		this.buttonCollectCoins = this.addRenderableWidget(IconAndButtonUtil.collectCoinButton(this.leftPos + 20, this.topPos - 20, this::CollectCoins, this.menu.player, this.menu::getSingleTrader));
		this.buttonOpenTerminal = this.addRenderableWidget(IconAndButtonUtil.backToTerminalButton(this.leftPos + TraderMenu.SLOT_OFFSET - 20, this.topPos + this.imageHeight - 20, this::OpenTerminal, this.menu::isUniversalTrader));
		
		//Trade Button Display
		this.tradeDisplay = this.addRenderableWidget(new TradeButtonArea(this.menu.traderSource, this.menu::getContext, this.leftPos + 3, this.topPos + 17, this.imageWidth - 6, 100, 2, this::addRenderableWidget, this::removeWidget, this::OnButtonPress, TradeButtonArea.FILTER_VALID));
		this.tradeDisplay.init();
		
		this.containerTick();
		
	}

	@Override
	protected void renderBg(PoseStack pose, float partialTicks, int mouseX, int mouseY) {
		
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		
		//Main BG
		this.blit(pose, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
		//Coin Slots
		for(Slot slot : this.menu.getCoinSlots())
		{
			this.blit(pose, this.leftPos + slot.x - 1, this.topPos + slot.y - 1, this.imageWidth, 0, 18, 18);
		}
		
		//Interaction Slot BG
		if(this.menu.getInteractionSlot().isActive())
			this.blit(pose, this.leftPos + this.menu.getInteractionSlot().x - 1, this.topPos + this.menu.getInteractionSlot().y - 1, this.imageWidth, 0, 18, 18);
		
	}
	
	@Override
	protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
		
		this.tradeDisplay.renderTraderName(pose, 8, 6, this.imageWidth - 16, false);
		
		this.font.draw(pose, this.playerInventoryTitle, TraderMenu.SLOT_OFFSET + 8, this.imageHeight - 94, 0x404040);
		
		//Moved to underneath the coin slots
		String valueText = MoneyUtil.getStringOfValue(this.menu.getContext(null).getAvailableFunds());
		font.draw(pose, valueText, TraderMenu.SLOT_OFFSET + 170 - this.font.width(valueText), this.imageHeight - 94, 0x404040);
		
	}
	
	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.renderBackground(pose);
		super.render(pose, mouseX, mouseY, partialTicks);
		this.renderTooltip(pose, mouseX, mouseY);
		
		if(this.menu.getCarried().isEmpty())
			this.tradeDisplay.renderTooltips(this, pose, this.leftPos + 8, this.topPos + 6, this.imageWidth - 16, mouseX, mouseY);
		
		IconAndButtonUtil.renderButtonTooltips(pose, mouseX, mouseY, this.renderables);
		
	}
	
	@Override
	public void containerTick() {
		this.tradeDisplay.tick();
	}
	
	private void OnButtonPress(ITrader trader, ITradeData trade) {
		
		if(trader == null || trade == null)
			return;
		
		ITraderSource ts = this.menu.traderSource.get();
		if(ts == null)
		{
			this.menu.player.closeContainer();
			return;
		}
		
		List<ITrader> traders = ts.getTraders();
		int ti = traders.indexOf(trader);
		if(ti < 0)
			return;
		
		ITrader t = traders.get(ti);
		if(t == null)
			return;
		
		int tradeIndex = t.getTradeInfo().indexOf(trade);
		if(tradeIndex < 0)
			return;
		
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageExecuteTrade(ti, tradeIndex));
		
	}
	
	private void OpenStorage(Button button) {
		if(this.menu.isSingleTrader())
			this.menu.getSingleTrader().sendOpenStorageMessage();
	}
	
	private void CollectCoins(Button button) {
		if(this.menu.isSingleTrader())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCollectCoins());
	}
	
	private void OpenTerminal(Button button) {
		if(this.menu.isUniversalTrader())
		{
			this.menu.player.closeContainer();
			LightmansCurrency.PROXY.openTerminalScreen();
		}
	}
	
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
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaMouseX, double deltaMouseY) {
		this.tradeDisplay.getScrollBar().onMouseDragged(mouseX, mouseY, button);
		return super.mouseDragged(mouseX, mouseY, button, deltaMouseX, deltaMouseY);
	}
	
}
