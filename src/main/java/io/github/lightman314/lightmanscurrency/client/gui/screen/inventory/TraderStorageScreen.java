package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import org.anti_ad.mc.ipn.api.IPNIgnore;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenTrades;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageStoreCoins;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@IPNIgnore
public class TraderStorageScreen extends EasyMenuScreen<TraderStorageMenu> {

	private final Map<Integer,TraderStorageClientTab<?>> availableTabs = new HashMap<>();
	public TraderStorageClientTab<?> currentTab() { return this.availableTabs.get(this.menu.getCurrentTabIndex()); }

	Map<Integer,TabButton> tabButtons = new HashMap<>();
	
	EasyButton buttonShowTrades;
	EasyButton buttonCollectMoney;

	EasyButton buttonStoreMoney;

	EasyButton buttonTradeRules;

	public final LazyWidgetPositioner leftEdgePositioner = LazyWidgetPositioner.create(this, LazyWidgetPositioner.MODE_BOTTOMUP, -20, TraderScreen.HEIGHT - 20, 20);

	public TraderStorageScreen(TraderStorageMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.resize(TraderScreen.WIDTH, TraderScreen.HEIGHT);
		this.menu.getAllTabs().forEach((key,tab) -> {
			try{
				Object t = tab.createClientTab(this);
				if(t instanceof TraderStorageClientTab<?> ct)
					this.availableTabs.put(key, ct);
				else
					LightmansCurrency.LogError("Common tab of type '" + tab.getClass().getName() + "' did not create a valid client tab!");
			} catch (Throwable t) { LightmansCurrency.LogError("Error initializing the Trader Storage Client Tabs!", t); }
		});
		if(this.availableTabs.size() == 0)
			LightmansCurrency.LogError("No client tabs were created for the Trader Storage Screen!");
		else
			LightmansCurrency.LogDebug("Storage Screen created with " + this.availableTabs.size() + " client tabs.");
		menu.addMessageListener(this::serverMessage);
	}
	
	@Override
	public void initialize(ScreenArea screenArea) {

		this.leftEdgePositioner.clear();
		this.addChild(this.leftEdgePositioner);

		//Create the tab buttons
		this.tabButtons.clear();
		this.availableTabs.forEach((key,tab) ->{
			TabButton newButton = this.addChild(new TabButton(button -> this.changeTab(key), tab));
			if(key == this.menu.getCurrentTabIndex())
				newButton.active = false;
			this.tabButtons.put(key, newButton);
		});
		if(this.availableTabs.size() == 0)
		{
			LightmansCurrency.LogError("NO CLIENT TABS WERE INITIALIZED!");
		}
		this.tickTabButtons();

		//Other buttons
		this.buttonShowTrades = this.addChild(IconAndButtonUtil.traderButton(0, 0, this::PressTradesButton));

		this.buttonCollectMoney = this.addChild(IconAndButtonUtil.collectCoinButton(0,0, this::PressCollectionButton, this.menu.player, this.menu::getTrader));

		this.buttonStoreMoney = this.addChild(IconAndButtonUtil.storeCoinButton(this.leftPos + 71, this.topPos + 120, this::PressStoreCoinsButton)
				.withAddons(EasyAddonHelper.visibleCheck(() -> this.menu.HasCoinsToAdd() && this.menu.hasPermission(Permissions.STORE_COINS) && this.menu.areCoinSlotsVisible())));

		this.buttonTradeRules = this.addChild(IconAndButtonUtil.tradeRuleButton(this.leftPos + this.imageWidth, this.topPos, this::PressTradeRulesButton)
				.withAddons(EasyAddonHelper.visibleCheck(() -> this.menu.hasPermission(Permissions.EDIT_TRADE_RULES) && this.currentTab().getTradeRuleTradeIndex() >= 0)));

		//Left side auto-position
		this.leftEdgePositioner.addWidgets(this.buttonShowTrades, this.buttonCollectMoney);

		TraderData trader = this.menu.getTrader();
		if(trader != null)
			trader.onStorageScreenInit(this, this::addChild);

		//Initialize the current tab
		this.currentTab().onOpen();

		this.containerTick();
		
	}

	private void tickTabButtons()
	{
		//Position the tab buttons
		int xPos = this.leftPos - TabButton.SIZE;
		AtomicInteger index = new AtomicInteger(0);
		this.tabButtons.forEach((key,button) -> {
			TraderStorageClientTab<?> tab = this.availableTabs.get(key);
			button.visible = tab != null && tab.tabButtonVisible() && tab.commonTab.canOpen(this.menu.player);
			if(button.visible)
			{
				int yPos = this.topPos + TabButton.SIZE * index.get();
				button.reposition(xPos, yPos, 3);
				index.set(index.get() + 1);
			}
		});
	}

	@Override
	protected void renderBG(@Nonnull EasyGuiGraphics gui) {

		if(this.menu.getTrader() == null)
		{
			this.onClose();
			return;
		}

		this.tickTabButtons();

		//Main BG
		gui.renderNormalBackground(TraderScreen.GUI_TEXTURE, this);
		
		//Coin Slots
		for(CoinSlot slot : this.menu.getCoinSlots())
		{
			if(slot.isActive())
				gui.blit(TraderScreen.GUI_TEXTURE, slot.x - 1, slot.y - 1, this.imageWidth, 0, 18, 18);
		}
		
		//Current tab
		if(this.currentTab() != null)
		{
			try { this.currentTab().renderBG(gui);
			} catch(Throwable t) { LightmansCurrency.LogError("Error rendering trader storage tab " + this.currentTab().getClass().getName(), t); }
		}

		//Labels
		if(this.currentTab().shouldRenderInventoryText())
			gui.drawString(this.playerInventoryTitle, TraderStorageMenu.SLOT_OFFSET + 8, this.imageHeight - 94, 0x404040);

	}

	@Override
	protected void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {
		try { this.currentTab().renderAfterWidgets(gui);
		} catch(Throwable t) { LightmansCurrency.LogError("Error rendering trader storage tab tooltips " + this.currentTab().getClass().getName(), t); }
	}
	
	@Override
	public void screenTick()
	{
		if(this.menu.getTrader() == null)
		{
			this.onClose();
			return;
		}
		
		this.menu.validateCoinSlots();
		
		if(!this.menu.hasPermission(Permissions.OPEN_STORAGE))
		{
			this.onClose();
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenTrades(this.menu.getTrader().getID()));
			return;
		}

		//Reset to the default tab if the currently selected tab doesn't have access permissions
		if(!this.currentTab().commonTab.canOpen(this.menu.player))
			this.changeTab(TraderStorageTab.TAB_TRADE_BASIC);
		
	}

	@Override
	public boolean blockInventoryClosing() { return this.currentTab().blockInventoryClosing(); }
	
	private TabButton getTabButton(int key) {
		if(this.tabButtons.containsKey(key))
			return this.tabButtons.get(key);
		return null;
	}

	public void changeTab(int newTab) { this.changeTab(newTab, true, null); }
	
	public void changeTab(int newTab, boolean sendMessage, @Nullable CompoundTag selfMessage) {
		
		if(newTab == this.menu.getCurrentTabIndex())
			return;
		
		//Close the old tab
		int oldTab = this.menu.getCurrentTabIndex();
		this.currentTab().onClose();
		
		//Make the old tabs button active again
		TabButton button = this.getTabButton(this.menu.getCurrentTabIndex());
		if(button != null)
			button.active = true;
		
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

	public void selfMessage(CompoundTag message) {
		//LightmansCurrency.LogInfo("Received self-message:\n" + message.getAsString());
		if(message.contains("ChangeTab",Tag.TAG_INT))
			this.changeTab(message.getInt("ChangeTab"), false, message);
		this.currentTab().receiveSelfMessage(message);
	}
	
	public void serverMessage(CompoundTag message) { this.currentTab().receiveServerMessage(message); }
	
	private void PressTradesButton(EasyButton button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenTrades(this.menu.getTrader().getID()));
	}
	
	private void PressCollectionButton(EasyButton button)
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
	
	private void PressStoreCoinsButton(EasyButton button)
	{
		if(this.menu.hasPermission(Permissions.STORE_COINS))
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageStoreCoins());
		}
		else
			Permissions.PermissionWarning(this.menu.player, "store coins", Permissions.STORE_COINS);
	}
	
	private void PressTradeRulesButton(EasyButton button)
	{
		if(this.currentTab().getTradeRuleTradeIndex() < 0)
			return;
		CompoundTag message = new CompoundTag();
		message.putInt("TradeIndex", this.currentTab().getTradeRuleTradeIndex());
		this.changeTab(TraderStorageTab.TAB_RULES_TRADE, true, message);
	}
	
}
