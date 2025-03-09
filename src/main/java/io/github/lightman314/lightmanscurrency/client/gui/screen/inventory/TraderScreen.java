package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import javax.annotation.Nonnull;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.traders.menu.customer.ITraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.util.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import io.github.lightman314.lightmanscurrency.network.message.trader.CPacketOpenNetworkTerminal;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.TraderClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.common.TraderInteractionTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.network.message.trader.CPacketCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.CPacketOpenStorage;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class TraderScreen extends EasyMenuScreen<TraderMenu> implements ITraderScreen {

	public static final ResourceLocation GUI_TEXTURE = VersionUtil.lcResource("textures/gui/container/trader.png");

	public static final int WIDTH = 206;
	public static final int HEIGHT = 236;

	private final ScreenPosition INFO_WIDGET_POSITION = ScreenPosition.of(TraderMenu.SLOT_OFFSET + 160, HEIGHT - 96);

	private final TraderClientTab DEFAULT_TAB = new TraderInteractionTab(this);

	IconButton buttonOpenStorage;
	IconButton buttonCollectCoins;

	IconButton buttonOpenTerminal;

	TraderClientTab currentTab = DEFAULT_TAB;
	@Override
	public void setTab(@Nonnull TraderClientTab tab) {
		//Close the old tab
		this.currentTab.onClose();
		//Set the new tab
		this.currentTab = tab;
		this.currentTab.onOpen();
	}
	public void closeTab() { this.setTab(DEFAULT_TAB); }

	private final LazyWidgetPositioner rightEdgePositioner = LazyWidgetPositioner.create(this, LazyWidgetPositioner.createTopdown(), TraderScreen.WIDTH,0, 20);

	@Override
	@Nonnull
	public IWidgetPositioner getRightEdgePositioner() { return this.rightEdgePositioner; }

	protected boolean forceShowTerminalButton() { return false; }

	public TraderScreen(TraderMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.resize(TraderScreen.WIDTH, TraderScreen.HEIGHT);
	}

	@Override
	public void initialize(ScreenArea screenArea) {

		this.rightEdgePositioner.clear();
		this.addChild(this.rightEdgePositioner);

		this.buttonOpenStorage = this.addChild(IconButton.builder()
				.pressAction(this::OpenStorage)
				.icon(IconUtil.ICON_STORAGE)
				.addon(EasyAddonHelper.visibleCheck(() -> this.menu.isSingleTrader() && this.menu.getSingleTrader().hasPermission(this.menu.player, Permissions.OPEN_STORAGE)))
				.addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRADER_OPEN_STORAGE))
				.build());
		this.buttonCollectCoins = this.addChild(IconAndButtonUtil.finishCollectCoinButton(IconButton.builder().pressAction(this::CollectCoins), this.menu.player, this.menu::getSingleTrader));
		this.buttonOpenTerminal = this.addChild(IconButton.builder()
				.pressAction(this::OpenTerminal)
				.icon(IconUtil.ICON_BACK)
				.addon(EasyAddonHelper.visibleCheck(this::showTerminalButton))
				.addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRADER_NETWORK_BACK))
				.build());
		this.buttonOpenTerminal.visible = this.showTerminalButton();

		this.rightEdgePositioner.addWidgets(this.buttonOpenTerminal, this.buttonOpenStorage, this.buttonCollectCoins);

		//Allow traders to add custom buttons if this is a single trader
		if(this.menu.isSingleTrader())
		{
			TraderData trader = this.menu.getSingleTrader();
			if(trader != null)
				trader.onScreenInit(this, this::addChild);
		}

		//Initialize the current tab
		this.currentTab.onOpen();

		this.containerTick();

	}

	private boolean showTerminalButton() { return this.forceShowTerminalButton() || (this.menu.isSingleTrader() && this.menu.getSingleTrader().showOnTerminal()); }

	@Override
	protected void renderBG(@Nonnull EasyGuiGraphics gui) {

		//Main BG
		gui.renderNormalBackground(GUI_TEXTURE, this);

		//Info widget
		gui.blit(GUI_TEXTURE, INFO_WIDGET_POSITION, this.imageWidth + 38, 0, 10, 10);

		//Coin Slots
		for(Slot slot : this.menu.getCoinSlots())
			gui.blit(GUI_TEXTURE, slot.x - 1, slot.y - 1, this.imageWidth, 0, 18, 18);


		//Interaction Slot BG
		if(this.menu.getInteractionSlot().isActive())
			gui.blit(GUI_TEXTURE, this.menu.getInteractionSlot().x - 1, this.menu.getInteractionSlot().y - 1, this.imageWidth, 0, 18, 18);

		try { this.currentTab.renderBG(gui);
		} catch(Throwable t) { LightmansCurrency.LogError("Error rendering trader tab " + this.currentTab.getClass().getName(), t); }

		//Labels
		gui.drawString(this.playerInventoryTitle, TraderMenu.SLOT_OFFSET + 8, this.imageHeight - 94, 0x404040);

		//Moved to underneath the coin slots
		Component valueText = this.menu.getContext(null).getAvailableFunds().getRandomValueText();
		gui.drawString(valueText, TraderMenu.SLOT_OFFSET + 170 - gui.font.width(valueText) - 10, this.imageHeight - 94, 0x404040);

	}

	@Override
	protected void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {
		try { this.currentTab.renderAfterWidgets(gui);
		} catch (Throwable t) { LightmansCurrency.LogError("Error rendering trader tab tooltips " + this.currentTab.getClass().getName(), t); }

		if(INFO_WIDGET_POSITION.offset(this).isMouseInArea(gui.mousePos, 10, 10))
			gui.renderComponentTooltip(this.menu.getContext(null).getAvailableFundsDescription());

	}

	private void OpenStorage(EasyButton button) {
		if(this.menu.isSingleTrader())
			new CPacketOpenStorage(this.menu.getSingleTrader().getID()).send();
	}

	private void CollectCoins(EasyButton button) {
		if(this.menu.isSingleTrader())
			CPacketCollectCoins.sendToServer();
	}

	private void OpenTerminal(EasyButton button) {
		if(this.showTerminalButton())
			new CPacketOpenNetworkTerminal().send();
	}

	@Override
	public boolean blockInventoryClosing() { return this.currentTab.blockInventoryClosing(); }

}