package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderInterfaceBlockEntity.ActiveMode;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.TraderInterfaceTab;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class TraderInterfaceScreen extends EasyMenuScreen<TraderInterfaceMenu> {

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/trader_interface.png");
	
	public static final int WIDTH = 206;
	public static final int HEIGHT = 236;
	
	Map<Integer,TraderInterfaceClientTab<?>> availableTabs = new HashMap<>();
	public TraderInterfaceClientTab<?> currentTab() { return this.availableTabs.get(this.menu.getCurrentTabIndex()); }
	
	Map<Integer,TabButton> tabButtons = new HashMap<>();
	
	IconButton modeToggle;
	
	IconButton onlineModeToggle;
	
	public TraderInterfaceScreen(TraderInterfaceMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.menu.getAllTabs().forEach((key,tab) -> this.availableTabs.put(key, tab.createClientTab(this)));
		this.resize(WIDTH, HEIGHT);
	}
	
	@Override
	public void initialize(ScreenArea screenArea) {
		
		//Create the tab buttons
		this.tabButtons.clear();
		this.availableTabs.forEach((key,tab) ->{
			TabButton newButton = this.addChild(new TabButton(button -> this.changeTab(key), tab));
			if(key == this.menu.getCurrentTabIndex())
				newButton.active = false;
			this.tabButtons.put(key, newButton);
		});
		
		this.modeToggle = this.addChild(new IconButton(screenArea.x + screenArea.width, screenArea.y, this::ToggleMode, () -> IconAndButtonUtil.GetIcon(this.menu.getBE().getMode()))
				.withAddons(EasyAddonHelper.tooltip(() -> this.getMode().getDisplayText())));
		
		this.onlineModeToggle = this.addChild(new IconButton(screenArea.x + screenArea.width, screenArea.y + 20, this::ToggleOnlineMode, () -> this.menu.getBE().isOnlineMode() ? IconAndButtonUtil.ICON_ONLINEMODE_TRUE : IconAndButtonUtil.ICON_ONLINEMODE_FALSE)
				.withAddons(EasyAddonHelper.tooltip(() -> EasyText.translatable("gui.lightmanscurrency.interface.onlinemode." + this.menu.getBE().isOnlineMode()))));
		
		//Initialize the current tab
		this.currentTab().onOpen();
		
		this.containerTick();
		
	}

	@Override
	protected void renderBG(@Nonnull EasyGuiGraphics gui) {
		
		//Main BG
		gui.renderNormalBackground(GUI_TEXTURE, this);

		//Current tab
		try { this.currentTab().renderBG(gui);
		} catch(Throwable t) { LightmansCurrency.LogError("Error rendering trader storage tab " + this.currentTab().getClass().getName(), t); }

		//Labels
		gui.drawString(this.playerInventoryTitle, TraderInterfaceMenu.SLOT_OFFSET + 8, this.getYSize() - 94, 0x404040);
		
	}

	@Override
	protected void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {
		//Current tab
		try{ this.currentTab().renderAfterWidgets(gui);
		} catch(Throwable t) { LightmansCurrency.LogError("Error rendering trader storage tab " + this.currentTab().getClass().getName(), t); }
	}
	
	@Override
	public void screenTick()
	{
		
		if(!this.currentTab().commonTab.canOpen(this.menu.player))
			this.changeTab(TraderInterfaceTab.TAB_INFO);
		
		this.updateTabs();
		
	}
	
	private ActiveMode getMode() {
		if(this.menu.getBE() != null)
			return this.menu.getBE().getMode();
		return ActiveMode.DISABLED;
	}
	
	private void ToggleMode(EasyButton button) { this.menu.changeMode(this.getMode().getNext()); }
	
	private void ToggleOnlineMode(EasyButton button) { this.menu.setOnlineMode(!this.menu.getBE().isOnlineMode()); }
	
	private void updateTabs() {
		//Position the tab buttons
		int yPos = this.topPos - TabButton.SIZE;
		AtomicInteger index = new AtomicInteger(0);
		this.tabButtons.forEach((key,button) -> {
			TraderInterfaceClientTab<?> tab = this.availableTabs.get(key);
			button.visible = tab.tabButtonVisible();
			if(button.visible)
			{
				int xPos = this.leftPos + TabButton.SIZE * index.get();
				button.reposition(xPos, yPos, 0);
				index.set(index.get() + 1);
			}
		});
	}

	@Override
	public boolean blockInventoryClosing() { return this.currentTab().blockInventoryClosing(); }

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
			this.menu.sendMessage(this.menu.createTabChangeMessage(newTab, null));
		
	}
	
}
