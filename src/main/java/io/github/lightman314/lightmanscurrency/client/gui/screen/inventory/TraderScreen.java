package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces.ITooltipSource;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import net.minecraft.client.gui.components.Button;
import org.anti_ad.mc.ipn.api.IPNIgnore;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.TraderClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.common.TraderInteractionTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.IScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenStorage;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

@IPNIgnore
public class TraderScreen extends AbstractContainerScreen<TraderMenu> implements IScreen {

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/trader.png");
	
	public static final int WIDTH = 206;
	public static final int HEIGHT = 236;

	private final ScreenPosition INFO_WIDGET_POSITION = ScreenPosition.of(TraderMenu.SLOT_OFFSET + 160, HEIGHT - 96);

	private final TraderClientTab DEFAULT_TAB = new TraderInteractionTab(this);
	
	IconButton buttonOpenStorage;
	IconButton buttonCollectCoins;

	IconButton buttonOpenTerminal;
	
	List<Renderable> tabRenderables = new ArrayList<>();
	List<GuiEventListener> tabListeners = new ArrayList<>();
	
	TraderClientTab currentTab = DEFAULT_TAB;
	public void setTab(@Nonnull TraderClientTab tab) {
		//Close the old tab
		this.currentTab.onClose();
		this.tabRenderables.clear();
		this.tabListeners.clear();
		//Set the new tab
		this.currentTab = tab;
		this.currentTab.onOpen();
	}
	public void closeTab() { this.setTab(DEFAULT_TAB); }
	
	private final List<Runnable> tickListeners = new ArrayList<>();

	public final LazyWidgetPositioner leftEdgePositioner = LazyWidgetPositioner.create(this, LazyWidgetPositioner.MODE_BOTTOMUP, -20, TraderScreen.HEIGHT - 20, 20);
	
	protected boolean forceShowTerminalButton() { return false; }

	public TraderScreen(TraderMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.imageWidth = WIDTH;
		this.imageHeight = HEIGHT;
	}
	
	@Override
	public void init() {
		
		super.init();
		
		this.tabRenderables.clear();
		this.tabListeners.clear();
		this.leftEdgePositioner.clear();
		
		this.buttonOpenStorage = this.addRenderableWidget(IconAndButtonUtil.storageButton(this.leftPos + TraderMenu.SLOT_OFFSET - 20, this.topPos + 118, this::OpenStorage, () -> this.menu.isSingleTrader() && this.menu.getSingleTrader().hasPermission(this.menu.player, Permissions.OPEN_STORAGE)));
		this.buttonCollectCoins = this.addRenderableWidget(IconAndButtonUtil.collectCoinButton(this.leftPos + TraderMenu.SLOT_OFFSET - 20, this.topPos + 138, this::CollectCoins, this.menu.player, this.menu::getSingleTrader));
		this.buttonOpenTerminal = this.addRenderableWidget(IconAndButtonUtil.backToTerminalButton(this.leftPos + TraderMenu.SLOT_OFFSET - 20, this.topPos + this.imageHeight - 20, this::OpenTerminal, this::showTerminalButton));

		this.leftEdgePositioner.addWidgets(this.buttonOpenStorage, this.buttonCollectCoins);

		//Allow traders to add custom buttons if this is a single trader
		if(this.menu.isSingleTrader())
		{
			TraderData trader = this.menu.getSingleTrader();
			if(trader != null)
				trader.onScreenInit(this, this::addRenderableWidget);
		}

		//Initialize the current tab
		this.currentTab.onOpen();
		
		this.containerTick();
		
	}
	
	private boolean showTerminalButton() {
		return this.forceShowTerminalButton() || (this.menu.isSingleTrader() && this.menu.getSingleTrader().showOnTerminal()); 
	}

	@Override
	protected void renderBg(@NotNull PoseStack pose, float partialTicks, int mouseX, int mouseY) {
		
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		
		//Main BG
		this.blit(pose, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

		//Info widget
		this.blit(pose, this.leftPos + INFO_WIDGET_POSITION.x, this.topPos + INFO_WIDGET_POSITION.y, this.imageWidth + 38, 0, 10, 10);

		//Coin Slots
		for(Slot slot : this.menu.getCoinSlots())
		{
			this.blit(pose, this.leftPos + slot.x - 1, this.topPos + slot.y - 1, this.imageWidth, 0, 18, 18);
		}


		//Interaction Slot BG
		if(this.menu.getInteractionSlot().isActive())
			this.blit(pose, this.leftPos + this.menu.getInteractionSlot().x - 1, this.topPos + this.menu.getInteractionSlot().y - 1, this.imageWidth, 0, 18, 18);
		
		try {
			this.currentTab.renderBG(pose, mouseX, mouseY, partialTicks);
			this.tabRenderables.forEach(widget -> widget.render(pose, mouseX, mouseY, partialTicks));
		} catch(Throwable t) { LightmansCurrency.LogError("Error rendering trader tab " + this.currentTab.getClass().getName(), t); }
		
	}
	
	@Override
	protected void renderLabels(@NotNull PoseStack pose, int mouseX, int mouseY) {
		
		this.font.draw(pose, this.playerInventoryTitle, TraderMenu.SLOT_OFFSET + 8, this.imageHeight - 94, 0x404040);
		
		//Moved to underneath the coin slots
		String valueText = MoneyUtil.getStringOfValue(this.menu.getContext(null).getAvailableFunds());
		font.draw(pose, valueText, TraderMenu.SLOT_OFFSET + 170 - this.font.width(valueText) - 10, this.imageHeight - 94, 0x404040);
		
	}
	
	@Override
	public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.renderBackground(pose);
		super.render(pose, mouseX, mouseY, partialTicks);
		this.renderTooltip(pose, mouseX, mouseY);
		
		try {
			this.currentTab.renderTooltips(pose, mouseX, mouseY);
		} catch (Throwable t) { LightmansCurrency.LogError("Error rendering trader tab tooltips " + this.currentTab.getClass().getName(), t); }

		if(INFO_WIDGET_POSITION.offset(this).isMouseInArea(mouseX, mouseY, 10, 10))
			this.renderComponentTooltip(pose, this.menu.getContext(null).getAvailableFundsDescription(), mouseX, mouseY);

		ITooltipSource.renderTooltips(this, pose, mouseX, mouseY);
		
	}
	
	@Override
	public void containerTick() {
		this.currentTab.tick();
		for(Runnable r : this.tickListeners) r.run();
	}
	
	private void OpenStorage(Button button) {
		if(this.menu.isSingleTrader())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage(this.menu.getSingleTrader().getID()));
	}
	
	private void CollectCoins(Button button) {
		if(this.menu.isSingleTrader())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCollectCoins());
	}
	
	private void OpenTerminal(Button button) {
		if(this.showTerminalButton())
		{
			this.menu.player.closeContainer();
			LightmansCurrency.PROXY.openTerminalScreen();
		}
	}
	
	@Override
	public boolean keyPressed(int p_97765_, int p_97766_, int p_97767_) {
		InputConstants.Key mouseKey = InputConstants.getKey(p_97765_, p_97766_);
		//Manually block closing by inventory key, to allow usage of all letters while typing player names, etc.
		assert this.minecraft != null;
		if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey) && this.currentTab.blockInventoryClosing()) {
		  	return true;
		}
		return super.keyPressed(p_97765_, p_97766_, p_97767_);
	}
	
	public <T extends Renderable> T addRenderableTabWidget(T widget) {
		this.tabRenderables.add(widget);
		if(widget instanceof GuiEventListener gl)
			this.addTabListener(gl);
		return widget;
	}
	
	public <T extends Renderable> void removeRenderableTabWidget(T widget) {
		this.tabRenderables.remove(widget);
		if(widget instanceof GuiEventListener gl)
			this.removeTabListener(gl);
	}
	
	public <T extends GuiEventListener> T addTabListener(T listener) {
		this.tabListeners.add(listener);
		return listener;
	}
	
	public <T extends GuiEventListener> void removeTabListener(T listener) {
		this.tabListeners.remove(listener);
	}
	
	@Override
	public @NotNull List<? extends GuiEventListener> children()
	{
		List<? extends GuiEventListener> coreListeners = super.children();
		List<GuiEventListener> listeners = Lists.newArrayList();
		listeners.addAll(coreListeners);
		listeners.addAll(this.tabListeners);
		return listeners;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		try {
			if(this.currentTab.mouseClicked(mouseX, mouseY, button))
				return true;
		} catch(Throwable ignored) {}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		try {
			if(this.currentTab.mouseReleased(mouseX, mouseY, button))
				return true;
		} catch(Throwable ignored) {}
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public void addTickListener(Runnable r) {
		this.tickListeners.add(r);
	}

}
