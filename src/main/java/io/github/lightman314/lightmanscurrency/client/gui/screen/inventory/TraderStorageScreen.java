package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageScreen;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed.AdvancedTabbedMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.util.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.WidgetRotation;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;

import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.network.message.trader.CPacketCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.CPacketOpenTrades;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class TraderStorageScreen extends AdvancedTabbedMenuScreen<ITraderStorageMenu,TraderStorageMenu,TraderStorageTab,ITraderStorageScreen> implements ITraderStorageScreen {
	
	EasyButton buttonShowTrades;

	EasyButton buttonTradeRules;

	private final LazyWidgetPositioner rightEdgePositioner = LazyWidgetPositioner.create(this, LazyWidgetPositioner.createTopdown(), TraderScreen.WIDTH, 0, 20);

	@Override
	@Nonnull
	public IWidgetPositioner getRightEdgePositioner() { return this.rightEdgePositioner; }

	public TraderStorageScreen(TraderStorageMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.resize(TraderScreen.WIDTH, TraderScreen.HEIGHT);
		menu.setMessageListener(this::serverMessage);
	}

	@Nonnull
	@Override
	protected IWidgetPositioner getTabButtonPositioner() {
		return LazyWidgetPositioner.create(this,LazyWidgetPositioner.createTopdown(WidgetRotation.LEFT),ScreenPosition.of(TabButton.NEGATIVE_SIZE,0), TabButton.SIZE);
	}

	protected TraderStorageClientTab<?> getCurrentTab() {
		if(this.currentTab() instanceof TraderStorageClientTab<?> tab)
			return tab;
		return null;
	}

	@Override
	public void init(ScreenArea screenArea) {

		this.rightEdgePositioner.clear();
		this.addChild(this.rightEdgePositioner);

		//Other buttons
		this.buttonShowTrades = this.addChild(IconButton.builder()
						.pressAction(this::PressTradesButton)
						.icon(IconUtil.ICON_TRADER)
						.addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRADER_OPEN_TRADES))
						.addon(EasyAddonHelper.visibleCheck(this::showRightEdgeWidgets))
						.build());

		this.buttonTradeRules = this.addChild(IconButton.builder()
						.position(screenArea.pos.offset(screenArea.width,0))
						.pressAction(this::PressTradeRulesButton)
						.icon(IconUtil.ICON_TRADE_RULES)
						.addon(EasyAddonHelper.visibleCheck(() -> this.menu.hasPermission(Permissions.EDIT_TRADE_RULES) && this.getCurrentTab().getTradeRuleTradeIndex() >= 0 && this.showRightEdgeWidgets()))
						.addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRADER_TRADE_RULES_TRADE))
						.build());

		this.rightEdgePositioner.addWidgets(this.buttonTradeRules,this.buttonShowTrades);

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
		
		if(!this.menu.hasPermission(Permissions.OPEN_STORAGE))
		{
			this.onClose();
			new CPacketOpenTrades(this.menu.getTrader().getID()).send();
		}
		
	}

	@Override
	public boolean showRightEdgeWidgets() { return this.getCurrentTab().showRightEdgeButtons(); }

	public void serverMessage(LazyPacketData message) { this.getCurrentTab().receiveServerMessage(message); }
	
	private void PressTradesButton(EasyButton button) { new CPacketOpenTrades(this.menu.getTrader().getID()).send(); }
	
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
	
	private void PressTradeRulesButton(EasyButton button)
	{
		if(this.getCurrentTab().getTradeRuleTradeIndex() < 0)
			return;
		this.ChangeTab(TraderStorageTab.TAB_RULES_TRADE, this.builder().setInt("TradeIndex", this.getCurrentTab().getTradeRuleTradeIndex()));
	}

}
