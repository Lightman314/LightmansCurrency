package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.traders.client.TraderClientHooks;
import io.github.lightman314.lightmanscurrency.api.traders.client.IClientScreenListener;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.ITraderStorageScreen;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.client.gui.tabbed.AdvancedTabbedMenuScreen;
import io.github.lightman314.lightmanscurrency.api.client.widgets.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.WidgetRotation;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;

import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nullable;

public class TraderStorageScreen extends AdvancedTabbedMenuScreen<ITraderStorageMenu,TraderStorageMenu,TraderStorageTab,ITraderStorageScreen> implements ITraderStorageScreen {
	
	EasyButton buttonShowTrades;

	EasyButton buttonTradeRules;

	private final LazyWidgetPositioner rightEdgePositioner = LazyWidgetPositioner.create(this, LazyWidgetPositioner.createTopdown(), TraderScreen.WIDTH, 0, 20);

	@Override
	public IWidgetPositioner getRightEdgePositioner() { return this.rightEdgePositioner; }

	public TraderStorageScreen(TraderStorageMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.resize(TraderScreen.WIDTH, TraderScreen.HEIGHT);
		menu.setMessageListener(this::serverMessage);
	}

	@Override
	protected IWidgetPositioner getTabButtonPositioner() {
		return LazyWidgetPositioner.create(this,LazyWidgetPositioner.createTopdown(WidgetRotation.LEFT),ScreenPosition.of(TabButton.NEGATIVE_SIZE,0), TabButton.SIZE);
	}

    @Override
	public TraderStorageClientTab<?> getCurrentTab() {
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

		this.rightEdgePositioner.addWidgets(this.buttonShowTrades);

		TraderData trader = this.menu.getTrader();
		if(trader != null)
            TraderClientHooks.forEach(trader,IClientScreenListener.class,attachment -> attachment.onStorageScreenInit(trader,this,this::addChild));

		//Initialize the current tab
		this.currentTab().onOpen();

		this.containerTick();
		
	}

	@Override
	protected void renderBackground(EasyGuiGraphics gui) {

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
            this.menu.openTrades();
		}
		
	}

	@Override
	public boolean showRightEdgeWidgets() { return this.getCurrentTab().showRightEdgeButtons(); }

    @Override
    public void ChangeTab(ResourceLocation tabKey) { this.ChangeTab(this.menu.getTabSlot(tabKey)); }
    @Override
    public void ChangeTab(ResourceLocation tabKey, @Nullable LazyPacketData.Builder data) { this.ChangeTab(this.menu.getTabSlot(tabKey),data); }
    @Override
    public void ChangeTab(ResourceLocation tabKey, @Nullable LazyPacketData data) { this.ChangeTab(this.menu.getTabSlot(tabKey),data,true); }

    public void serverMessage(LazyPacketData message) { this.getCurrentTab().receiveServerMessage(message); }
	
	private void PressTradesButton(EasyButton button) { this.menu.SendMessage(this.builder().setFlag("OpenTrades")); }

}
