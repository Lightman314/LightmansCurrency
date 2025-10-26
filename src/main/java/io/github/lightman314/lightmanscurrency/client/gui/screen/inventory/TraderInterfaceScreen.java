package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed.EasyTabbedMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.util.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.WidgetRotation;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;

import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity.ActiveMode;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceTab;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TraderInterfaceScreen extends EasyTabbedMenuScreen<TraderInterfaceMenu,TraderInterfaceTab,TraderInterfaceScreen> {

	public static final ResourceLocation GUI_TEXTURE = VersionUtil.lcResource("textures/gui/container/trader_interface.png");
	
	public static final int WIDTH = 206;
	public static final int HEIGHT = 236;
	
	IconButton modeToggle;
	
	IconButton onlineModeToggle;
	
	public TraderInterfaceScreen(TraderInterfaceMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.resize(WIDTH, HEIGHT);
	}

	@Override
	protected IWidgetPositioner getTabButtonPositioner() {
		return LazyWidgetPositioner.create(this,LazyWidgetPositioner.createLeftRight(WidgetRotation.TOP),ScreenPosition.of(0,TabButton.NEGATIVE_SIZE), TabButton.SIZE);
	}

	@Override
	public void init(ScreenArea screenArea) {
		
		this.modeToggle = this.addChild(IconButton.builder()
				.position(screenArea.pos.offset(screenArea.width,0))
				.pressAction(this::ToggleMode)
				.icon(() -> IconUtil.GetIcon(this.getMode()))
				.addon(EasyAddonHelper.tooltip(() -> this.getMode().getDisplayText()))
				.build());
		
		this.onlineModeToggle = this.addChild(IconButton.builder()
				.position(screenArea.pos.offset(screenArea.width,20))
				.pressAction(this::ToggleOnlineMode)
				.icon(() -> this.menu.getBE().isOnlineMode() ? IconUtil.ICON_ONLINEMODE_TRUE : IconUtil.ICON_ONLINEMODE_FALSE)
				.addon(EasyAddonHelper.tooltip(() -> this.menu.getBE().isOnlineMode() ? LCText.TOOLTIP_INTERFACE_ONLINE_MODE_ON.get() : LCText.TOOLTIP_INTERFACE_ONLINE_MODE_OFF.get()))
				.build());
		
		//Initialize the current tab
		this.currentTab().onOpen();
		
		this.containerTick();
		
	}

	@Override
	protected void renderBackground(EasyGuiGraphics gui) {
		
		//Main BG
		gui.renderNormalBackground(GUI_TEXTURE, this);

		//Labels
		gui.drawString(this.playerInventoryTitle, TraderInterfaceMenu.SLOT_OFFSET + 8, this.getYSize() - 94, 0x404040);
		
	}
	
	@Override
	public void screenTick()
	{
		
		if(!this.currentTab().commonTab.canOpen(this.menu.player))
			this.changeTab(TraderInterfaceTab.TAB_INFO);
		
	}
	
	private ActiveMode getMode() {
		if(this.menu.getBE() != null)
			return this.menu.getBE().getMode();
		return ActiveMode.DISABLED;
	}
	
	private void ToggleMode(EasyButton button) { this.menu.changeMode(this.getMode().getNext()); }
	
	private void ToggleOnlineMode(EasyButton button) { this.menu.setOnlineMode(!this.menu.getBE().isOnlineMode()); }

	@Override
	public boolean blockInventoryClosing() { return this.currentTab().blockInventoryClosing(); }
	
	public void changeTab(int newTab) { this.changeTab(newTab, true); }
	
	public void changeTab(int newTab, boolean sendMessage) {

		this.menu.ChangeTab(newTab);
		
	}

}
