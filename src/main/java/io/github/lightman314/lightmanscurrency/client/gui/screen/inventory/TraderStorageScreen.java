package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.anti_ad.mc.ipn.api.IPNIgnore;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.notifications.NotificationDisplayWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.IScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu.IClientMessage;
import io.github.lightman314.lightmanscurrency.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenTrades;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageStoreCoins;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

@IPNIgnore
public class TraderStorageScreen extends AbstractContainerScreen<TraderStorageMenu> implements IClientMessage, IScreen {

	Map<Integer,TraderStorageClientTab<?>> availableTabs = new HashMap<>();
	public TraderStorageClientTab<?> currentTab() { return this.availableTabs.get(this.menu.getCurrentTabIndex()); }
	
	Map<Integer,TabButton> tabButtons = new HashMap<>();
	
	Button buttonShowTrades;
	Button buttonCollectMoney;
	
	Button buttonOpenSettings;
	
	Button buttonStoreMoney;
	
	Button buttonShowLog;
	
	NotificationDisplayWidget logWindow;
	
	Button buttonTradeRules;
	
	List<AbstractWidget> tabRenderables = new ArrayList<>();
	List<GuiEventListener> tabListeners = new ArrayList<>();
	
	private final List<Runnable> tickListeners = new ArrayList<>();
	
	public TraderStorageScreen(TraderStorageMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.menu.getAllTabs().forEach((key,tab) -> this.availableTabs.put(key, tab.createClientTab(this)));
		this.imageWidth = TraderScreen.WIDTH;
		this.imageHeight = TraderScreen.HEIGHT;
		menu.addMessageListener(this::serverMessage);
	}
	
	@Override
	public void init() {
		
		super.init();
		
		this.tabRenderables.clear();
		this.tabListeners.clear();
		
		//Create the tab buttons
		this.tabButtons.clear();
		this.availableTabs.forEach((key,tab) ->{
			if(tab.tabButtonVisible()) {
				TabButton newButton = this.addRenderableWidget(new TabButton(button -> this.changeTab(key), this.font, tab));
				if(key == this.menu.getCurrentTabIndex())
					newButton.active = false;
				this.tabButtons.put(key, newButton);
			}
		});
		//Position the tab buttons
		int xPos = this.leftPos - TabButton.SIZE;
		AtomicInteger index = new AtomicInteger(0);
		this.tabButtons.forEach((key,button) -> {
			int yPos = this.topPos + TabButton.SIZE * index.get();
			button.reposition(xPos, yPos, 3);
			index.set(index.get() + 1);
		});
		
		//Other buttons
		this.buttonShowTrades = this.addRenderableWidget(IconAndButtonUtil.traderButton(this.leftPos + TraderStorageMenu.SLOT_OFFSET - 20, this.topPos + 118, this::PressTradesButton));
		
		this.buttonCollectMoney = this.addRenderableWidget(IconAndButtonUtil.collectCoinButton(this.leftPos + TraderStorageMenu.SLOT_OFFSET - 20, this.topPos + 138, this::PressCollectionButton, this.menu.player, this.menu::getTrader));
		this.buttonCollectMoney.visible = this.menu.hasPermission(Permissions.COLLECT_COINS) && !this.menu.getTrader().hasBankAccount();
		
		this.buttonStoreMoney = this.addRenderableWidget(IconAndButtonUtil.storeCoinButton(this.leftPos + TraderStorageMenu.SLOT_OFFSET + 176, this.topPos + 158, this::PressStoreCoinsButton));
		this.buttonStoreMoney.visible = false;
		
		this.buttonOpenSettings = this.addRenderableWidget(IconAndButtonUtil.openSettingsButton(this.leftPos + TraderStorageMenu.SLOT_OFFSET + 176, this.topPos + 118, this::PressSettingsButton));
		this.buttonOpenSettings.visible = this.menu.hasPermission(Permissions.EDIT_SETTINGS);
		
		this.buttonTradeRules = this.addRenderableWidget(IconAndButtonUtil.tradeRuleButton(this.leftPos + TraderStorageMenu.SLOT_OFFSET + 176, this.topPos + 138, this::PressTradeRulesButton, () -> this.currentTab().getTradeRuleTradeIndex() >= 0));
		this.buttonTradeRules.visible = this.menu.hasPermission(Permissions.EDIT_TRADE_RULES);
		
		this.buttonShowLog = this.addRenderableWidget(IconAndButtonUtil.showLoggerButton(this.leftPos + TraderStorageMenu.SLOT_OFFSET - 20, this.topPos + 158, this::PressLogButton, () -> false));
		
		this.logWindow = this.addRenderableWidget(new NotificationDisplayWidget(this.leftPos + TraderStorageMenu.SLOT_OFFSET, this.topPos, this.imageWidth - (2 * TraderStorageMenu.SLOT_OFFSET), this.imageHeight / NotificationDisplayWidget.HEIGHT_PER_ROW, this.font, () -> { TraderData trader = this.menu.getTrader(); return trader != null ? trader.getNotifications() : new ArrayList<>(); }));
		this.logWindow.visible = false;
		
		//Left side auto-position
		LazyWidgetPositioner.create(this, LazyWidgetPositioner.MODE_TOPDOWN, TraderMenu.SLOT_OFFSET - 20, 118, 20, this.buttonShowTrades, this.buttonCollectMoney, this.buttonShowLog);
		//Right side auto-position
		LazyWidgetPositioner.create(this, LazyWidgetPositioner.MODE_TOPDOWN, TraderStorageMenu.SLOT_OFFSET + 176, 118, 20, this.buttonStoreMoney, this.buttonOpenSettings, this.buttonTradeRules);
		
		//Initialize the current tab
		this.currentTab().onOpen();
		
		this.containerTick();
		
	}

	@Override
	protected void renderBg(@NotNull PoseStack pose, float partialTicks, int mouseX, int mouseY) {
		
		RenderSystem.setShaderTexture(0, TraderScreen.GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		
		//Main BG
		this.blit(pose, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
		//Coin Slots
		for(CoinSlot slot : this.menu.getCoinSlots())
		{
			if(slot.isActive())
				this.blit(pose, this.leftPos + slot.x - 1, this.topPos + slot.y - 1, this.imageWidth, 0, 18, 18);
		}
		
		//Current tab
		try {
			this.currentTab().renderBG(pose, mouseX, mouseY, partialTicks);
			this.tabRenderables.forEach(widget -> widget.render(pose, mouseX, mouseY, partialTicks));
		} catch(Exception e) { LightmansCurrency.LogError("Error rendering trader storage tab " + this.currentTab().getClass().getName(), e); }
		
		
	}
	
	@Override
	protected void renderLabels(@NotNull PoseStack pose, int mouseX, int mouseY) {
		
		this.font.draw(pose, this.playerInventoryTitle, TraderMenu.SLOT_OFFSET + 8, this.imageHeight - 94, 0x404040);
		
	}
	
	@Override
	public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		if(this.menu.getTrader() == null)
		{
			this.menu.player.closeContainer();
			return;
		}
		
		this.renderBackground(pose);
		if(this.logWindow != null && this.logWindow.visible)
		{
			this.logWindow.render(pose, mouseX, mouseY, partialTicks);
			this.buttonShowLog.render(pose, mouseX, mouseY, partialTicks);
			IconAndButtonUtil.ManuallyRenderTooltips(this, pose, mouseX, mouseY, Lists.newArrayList(this.buttonShowLog));
			this.logWindow.tryRenderTooltip(pose, this, mouseX, mouseY);
			return;
		}
		super.render(pose, mouseX, mouseY, partialTicks);
		this.renderTooltip(pose, mouseX, mouseY);
		
		try {
			this.currentTab().renderTooltips(pose, mouseX, mouseY);
		} catch(Exception e) { LightmansCurrency.LogError("Error rendering trader storage tab tooltips " + this.currentTab().getClass().getName(), e); }
		
		//IconAndButtonUtil.renderButtonTooltips(pose, mouseX, mouseY, this.renderables);
		
		this.tabButtons.forEach((key, button) ->{
			if(button.isMouseOver(mouseX, mouseY))
				this.renderTooltip(pose, button.tab.getTooltip(), mouseX, mouseY);
		});
		
	}
	
	@Override
	public void containerTick()
	{
		if(this.menu.getTrader() == null)
		{
			this.menu.player.closeContainer();
			return;
		}
		
		this.menu.validateCoinSlots();
		
		if(!this.menu.hasPermission(Permissions.OPEN_STORAGE))
		{
			this.menu.player.closeContainer();
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenTrades(this.menu.getTrader().getID()));
			return;
		}
		
		this.buttonOpenSettings.visible = this.menu.hasPermission(Permissions.EDIT_SETTINGS);
		this.buttonTradeRules.visible = this.menu.hasPermission(Permissions.EDIT_TRADE_RULES);
		
		this.buttonStoreMoney.visible = this.menu.HasCoinsToAdd() && this.menu.hasPermission(Permissions.STORE_COINS);
		this.buttonShowLog.visible = this.menu.hasPermission(Permissions.VIEW_LOGS);
		
		this.currentTab().tick();
		
		for(Runnable r : this.tickListeners) r.run();
		
	}
	
	@Override
	public boolean keyPressed(int p_97765_, int p_97766_, int p_97767_) {
	      InputConstants.Key mouseKey = InputConstants.getKey(p_97765_, p_97766_);
		  assert this.minecraft != null;
	      //Manually block closing by inventory key, to allow usage of all letters while typing player names, etc.
	      if (this.currentTab().blockInventoryClosing() && this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) { return true; }
	      return super.keyPressed(p_97765_, p_97766_, p_97767_);
	}
	
	private TabButton getTabButton(int key) {
		if(this.tabButtons.containsKey(key))
			return this.tabButtons.get(key);
		return null;
	}
	
	public void changeTab(int newTab) { this.changeTab(newTab, true, null); }
	
	public void changeTab(int newTab, boolean sendMessage, CompoundTag selfMessage) {
		
		if(newTab == this.menu.getCurrentTabIndex())
			return;
		
		//Close the old tab
		int oldTab = this.menu.getCurrentTabIndex();
		this.currentTab().onClose();
		
		//Make the old tabs button active again
		TabButton button = this.getTabButton(this.menu.getCurrentTabIndex());
		if(button != null)
			button.active = true;
		
		//Clear the renderables & listeners
		this.tabRenderables.clear();
		this.tabListeners.clear();
		
		//Change the tab officially
		this.menu.changeTab(newTab);
		
		//Make the tab button for the current tab inactive
		button = this.getTabButton(this.menu.getCurrentTabIndex());
		if(button != null)
			button.active = false;
		
		//Open the new tab
		if(selfMessage != null)
			this.currentTab().receiveSelfMessage(selfMessage);
		this.currentTab().onOpen();
		
		//Inform the server that the tab has been changed
		if(oldTab != this.menu.getCurrentTabIndex() && sendMessage)
			this.menu.sendMessage(this.menu.createTabChangeMessage(newTab, selfMessage));
		
	}
	
	@Override
	public void selfMessage(CompoundTag message) {
		//LightmansCurrency.LogInfo("Received self-message:\n" + message.getAsString());
		if(message.contains("ChangeTab",Tag.TAG_INT))
			this.changeTab(message.getInt("ChangeTab"), false, message);
		else
			this.currentTab().receiveSelfMessage(message);
	}
	
	public void serverMessage(CompoundTag message) {
		this.currentTab().receiveServerMessage(message);
	}
	
	public <T extends AbstractWidget> T addRenderableTabWidget(T widget) {
		this.tabRenderables.add(widget);
		return widget;
	}
	
	public <T extends Renderable> void removeRenderableTabWidget(T widget) {
		this.tabRenderables.remove(widget);
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
		listeners.addAll(this.tabRenderables);
		listeners.addAll(this.tabListeners);
		return listeners;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		try {
			if(this.currentTab().mouseClicked(mouseX, mouseY, button))
				return true;
		} catch(Throwable ignored) {}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		try {
			if(this.currentTab().mouseReleased(mouseX, mouseY, button))
				return true;
		} catch(Throwable ignored) {}
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	private void PressTradesButton(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenTrades(this.menu.getTrader().getID()));
	}
	
	private void PressCollectionButton(Button button)
	{
		//Open the container screen
		if(this.menu.hasPermission(Permissions.COLLECT_COINS))
		{
			//CurrencyMod.LOGGER.info("Owner attempted to collect the stored money.");
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCollectCoins());
		}
		else
			Permissions.PermissionWarning(this.menu.player, "collect stored coins", Permissions.COLLECT_COINS);
	}
	
	private void PressStoreCoinsButton(Button button)
	{
		if(this.menu.hasPermission(Permissions.STORE_COINS))
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageStoreCoins());
		}
		else
			Permissions.PermissionWarning(this.menu.player, "store coins", Permissions.STORE_COINS);
	}
	
	private void PressLogButton(Button button)
	{
		this.logWindow.visible = !this.logWindow.visible;
	}
	
	private void PressTradeRulesButton(Button button)
	{
		this.menu.player.closeContainer();
		Minecraft.getInstance().setScreen(new TradeRuleScreen(this.menu.getTrader().getID(), this.currentTab().getTradeRuleTradeIndex()));
	}
	
	private void PressSettingsButton(Button button)
	{
		this.menu.player.closeContainer();
		Minecraft.getInstance().setScreen(new TraderSettingsScreen(this.menu.traderSource));
	}

	@Override
	public void addTickListener(Runnable r) {
		this.tickListeners.add(r);
	}
	
}
