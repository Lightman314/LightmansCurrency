package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.client.util.RenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.anti_ad.mc.ipn.api.IPNIgnore;

import com.google.common.collect.Lists;

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

@IPNIgnore
public class TraderScreen extends ContainerScreen<TraderMenu> implements IScreen {

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/trader.png");

	public static final int WIDTH = 206;
	public static final int HEIGHT = 236;

	private final ScreenPosition INFO_WIDGET_POSITION = ScreenPosition.of(TraderMenu.SLOT_OFFSET + 160, HEIGHT - 96);

	private final TraderClientTab DEFAULT_TAB = new TraderInteractionTab(this);

	IconButton buttonOpenStorage;
	IconButton buttonCollectCoins;

	IconButton buttonOpenTerminal;

	List<IRenderable> tabRenderables = new ArrayList<>();
	List<IGuiEventListener> tabListeners = new ArrayList<>();

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

	protected boolean forceShowTerminalButton() { return false; }

	public TraderScreen(TraderMenu menu, PlayerInventory inventory, ITextComponent title) {
		super(menu, inventory, title);
		this.imageWidth = WIDTH;
		this.imageHeight = HEIGHT;
	}

	@Override
	public void init() {

		super.init();

		this.tabRenderables.clear();
		this.tabListeners.clear();

		this.buttonOpenStorage = this.addButton(IconAndButtonUtil.storageButton(this.leftPos + TraderMenu.SLOT_OFFSET - 20, this.topPos + 118, this::OpenStorage, () -> this.menu.isSingleTrader() && this.menu.getSingleTrader().hasPermission(this.menu.player, Permissions.OPEN_STORAGE)));
		this.buttonCollectCoins = this.addButton(IconAndButtonUtil.collectCoinButton(this.leftPos + TraderMenu.SLOT_OFFSET - 20, this.topPos + 138, this::CollectCoins, this.menu.player, this.menu::getSingleTrader));
		this.buttonOpenTerminal = this.addButton(IconAndButtonUtil.backToTerminalButton(this.leftPos + TraderMenu.SLOT_OFFSET - 20, this.topPos + this.imageHeight - 20, this::OpenTerminal, this::showTerminalButton));

		LazyWidgetPositioner.create(this, LazyWidgetPositioner.MODE_TOPDOWN, TraderMenu.SLOT_OFFSET - 20, 118, 20, this.buttonOpenStorage, this.buttonCollectCoins);

		//Initialize the current tab
		this.currentTab.onOpen();

		this.tick();

	}

	private boolean showTerminalButton() {
		return this.forceShowTerminalButton() || (this.menu.isSingleTrader() && this.menu.getSingleTrader().showOnTerminal());
	}

	@Override
	protected void renderBg(@Nonnull MatrixStack pose, float partialTicks, int mouseX, int mouseY) {

		RenderUtil.bindTexture(GUI_TEXTURE);
		RenderUtil.color4f(1f, 1f, 1f, 1f);

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
	protected void renderLabels(@Nonnull MatrixStack pose, int mouseX, int mouseY) {

		this.font.draw(pose, this.inventory.getName(), TraderMenu.SLOT_OFFSET + 8, this.imageHeight - 94, 0x404040);

		//Moved to underneath the coin slots
		String valueText = MoneyUtil.getStringOfValue(this.menu.getContext(null).getAvailableFunds());
		font.draw(pose, valueText, TraderMenu.SLOT_OFFSET + 170 - this.font.width(valueText) - 10, this.imageHeight - 94, 0x404040);

	}

	@Override
	public void render(@Nonnull MatrixStack pose, int mouseX, int mouseY, float partialTicks) {

		this.renderBackground(pose);
		super.render(pose, mouseX, mouseY, partialTicks);
		this.renderTooltip(pose, mouseX, mouseY);

		try {
			this.currentTab.renderTooltips(pose, mouseX, mouseY);
		} catch (Throwable t) { LightmansCurrency.LogError("Error rendering trader tab tooltips " + this.currentTab.getClass().getName(), t); }

		if(INFO_WIDGET_POSITION.offset(this).isMouseInArea(mouseX, mouseY, 10, 10))
			this.renderComponentTooltip(pose, this.menu.getContext(null).getAvailableFundsDescription(), mouseX, mouseY);

		IconAndButtonUtil.renderButtonTooltips(pose, mouseX, mouseY, this.buttons);

	}

	@Override
	public void tick() {
		super.tick();
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
		InputMappings.Input mouseKey = InputMappings.getKey(p_97765_, p_97766_);
		//Manually block closing by inventory key, to allow usage of all letters while typing player names, etc.
		assert this.minecraft != null;
		if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey) && this.currentTab.blockInventoryClosing()) {
			return true;
		}
		return super.keyPressed(p_97765_, p_97766_, p_97767_);
	}

	public <T extends IRenderable> T addRenderableTabWidget(T widget) {
		this.tabRenderables.add(widget);
		if(widget instanceof IGuiEventListener)
			this.addTabListener((IGuiEventListener)widget);
		return widget;
	}

	public <T extends IRenderable> void removeRenderableTabWidget(T widget) {
		this.tabRenderables.remove(widget);
		if(widget instanceof IGuiEventListener)
			this.removeTabListener((IGuiEventListener)widget);
	}

	public <T extends IGuiEventListener> T addTabListener(T listener) {
		this.tabListeners.add(listener);
		return listener;
	}

	public <T extends IGuiEventListener> void removeTabListener(T listener) {
		this.tabListeners.remove(listener);
	}

	@Override
	public @Nonnull List<? extends IGuiEventListener> children()
	{
		List<? extends IGuiEventListener> coreListeners = super.children();
		List<IGuiEventListener> listeners = Lists.newArrayList();
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