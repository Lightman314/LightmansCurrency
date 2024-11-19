package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.menus.MoneySlot;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageScreen;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed.AdvancedTabbedMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.util.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.WidgetRotation;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;

import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import io.github.lightman314.lightmanscurrency.network.message.trader.CPacketCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.CPacketOpenTrades;
import io.github.lightman314.lightmanscurrency.network.message.trader.CPacketStoreCoins;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TraderStorageScreen extends AdvancedTabbedMenuScreen<ITraderStorageMenu,TraderStorageMenu,TraderStorageTab,ITraderStorageScreen> implements ITraderStorageScreen {
	
	EasyButton buttonShowTrades;
	EasyButton buttonCollectMoney;

	EasyButton buttonStoreMoney;

	EasyButton buttonTradeRules;

	public final LazyWidgetPositioner leftEdgePositioner = LazyWidgetPositioner.create(this, LazyWidgetPositioner.createBottomup(), -20, TraderScreen.HEIGHT - 20, 20);

	public TraderStorageScreen(TraderStorageMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.resize(TraderScreen.WIDTH, TraderScreen.HEIGHT);
		menu.setMessageListener(this::serverMessage);
	}

	@Nonnull
	@Override
	protected IWidgetPositioner getTabButtonPositioner() {
		return LazyWidgetPositioner.create(this,LazyWidgetPositioner.createTopdown(WidgetRotation.LEFT),ScreenPosition.of(-25,0),25);
	}

	protected TraderStorageClientTab<?> getCurrentTab() {
		if(this.currentTab() instanceof TraderStorageClientTab<?> tab)
			return tab;
		return null;
	}

	@Override
	public void init(ScreenArea screenArea) {

		this.leftEdgePositioner.clear();
		this.addChild(this.leftEdgePositioner);

		//Other buttons
		this.buttonShowTrades = this.addChild(IconButton.builder()
						.pressAction(this::PressTradesButton)
						.icon(IconUtil.ICON_TRADER)
						.build());

		this.buttonCollectMoney = this.addChild(IconAndButtonUtil.finishCollectCoinButton(IconButton.builder().pressAction(this::PressCollectionButton), this.menu.player, this.menu::getTrader));

		this.buttonStoreMoney = this.addChild(IconButton.builder()
						.position(screenArea.pos.offset(71,120))
						.pressAction(this::PressStoreCoinsButton)
						.icon(IconUtil.ICON_STORE_COINS)
						.addon(EasyAddonHelper.visibleCheck(() -> this.menu.HasCoinsToAdd() && this.menu.hasPermission(Permissions.STORE_COINS) && this.menu.areCoinSlotsVisible()))
						.build());

		this.buttonTradeRules = this.addChild(IconButton.builder()
						.position(screenArea.pos.offset(screenArea.width,0))
						.pressAction(this::PressTradeRulesButton)
						.icon(IconUtil.ICON_TRADE_RULES)
						.addon(EasyAddonHelper.visibleCheck(() -> this.menu.hasPermission(Permissions.EDIT_TRADE_RULES) && this.getCurrentTab().getTradeRuleTradeIndex() >= 0))
						.addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRADER_TRADE_RULES_TRADE))
						.build());

		//Left side auto-position
		this.leftEdgePositioner.addWidgets(this.buttonShowTrades, this.buttonCollectMoney);

		TraderData trader = this.menu.getTrader();
		if(trader != null)
			trader.onStorageScreenInit(this, this::addChild);

		//Initialize the current tab
		this.currentTab().onOpen();

		this.containerTick();
		
	}

	@Override
	protected void renderBackground(@Nonnull EasyGuiGraphics gui) {

		if(this.menu.getTrader() == null)
		{
			this.onClose();
			return;
		}

		//Main BG
		gui.renderNormalBackground(TraderScreen.GUI_TEXTURE, this);
		
		//Coin Slots
		for(MoneySlot slot : this.menu.getCoinSlots())
		{
			if(slot.isActive())
				gui.blit(TraderScreen.GUI_TEXTURE, slot.x - 1, slot.y - 1, this.imageWidth, 0, 18, 18);
		}

		//Labels
		if(this.getCurrentTab().shouldRenderInventoryText())
			gui.drawString(this.playerInventoryTitle, TraderStorageMenu.SLOT_OFFSET + 8, this.imageHeight - 94, 0x404040);

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
			new CPacketOpenTrades(this.menu.getTrader().getID()).send();
		}
		
	}

	@Deprecated
	@Override
	public void changeTab(int newTab) { this.ChangeTab(newTab); }

	@Deprecated
	@Override
	public void changeTab(int newTab, boolean sendMessage, @Nullable LazyPacketData.Builder selfMessage) {
		this.ChangeTab(newTab,selfMessage != null ? selfMessage.build() : null, sendMessage);
	}

	public void serverMessage(LazyPacketData message) { this.getCurrentTab().receiveServerMessage(message); }
	
	private void PressTradesButton(EasyButton button)
	{
		new CPacketOpenTrades(this.menu.getTrader().getID()).send();
	}
	
	private void PressCollectionButton(EasyButton button)
	{
		//Open the container screen
		if(this.menu.hasPermission(Permissions.COLLECT_COINS))
		{
			//CurrencyMod.LOGGER.info("Owner attempted to collect the stored money.");
			CPacketCollectCoins.sendToServer();
		}
		else
			Permissions.PermissionWarning(this.menu.player, "collect stored coins", Permissions.COLLECT_COINS);
	}
	
	private void PressStoreCoinsButton(EasyButton button)
	{
		if(this.menu.hasPermission(Permissions.STORE_COINS))
			CPacketStoreCoins.sendToServer();
		else
			Permissions.PermissionWarning(this.menu.player, "store coins", Permissions.STORE_COINS);
	}
	
	private void PressTradeRulesButton(EasyButton button)
	{
		if(this.getCurrentTab().getTradeRuleTradeIndex() < 0)
			return;
		this.ChangeTab(TraderStorageTab.TAB_RULES_TRADE, this.builder().setInt("TradeIndex", this.getCurrentTab().getTradeRuleTradeIndex()));
	}

}
