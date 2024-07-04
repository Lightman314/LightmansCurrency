package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm.*;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.ATMMenu;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;

import javax.annotation.Nonnull;

public class ATMScreen extends EasyMenuScreen<ATMMenu> {

	public static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "textures/gui/container/atm.png");
	
	public static final ResourceLocation BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "textures/gui/container/atm_buttons.png");
	
	int currentTabIndex = 0;
	List<ATMTab> tabs = ImmutableList.of(new ExchangeTab(this), new SelectionTab(this), new InteractionTab(this), new NotificationTab(this), new LogTab(this), new TransferTab(this));
	public List<ATMTab> getTabs() { return this.tabs; }
	public ATMTab currentTab() { return tabs.get(this.currentTabIndex); }

	List<TabButton> tabButtons = new ArrayList<>();
	
	boolean logError = true;
	
	public ATMScreen(ATMMenu container, Inventory inventory, Component title) { super(container, inventory, title); this.resize(176, 243); }

	@Override
	protected void initialize(ScreenArea screenArea)
	{

		this.tabButtons = new ArrayList<>();
		for(int i = 0; i < this.tabs.size(); ++i)
		{
			TabButton button = this.addChild(new TabButton(this::clickedOnTab, this.tabs.get(i)));
			button.reposition(this.leftPos - TabButton.SIZE, this.topPos + i * TabButton.SIZE, 3);
			button.active = i != this.currentTabIndex;
			this.tabButtons.add(button);
		}

		this.currentTab().onOpen();

	}

	@Override
	protected void renderBG(@Nonnull EasyGuiGraphics gui)
	{

		gui.renderNormalBackground(GUI_TEXTURE, this);
		
		try { this.currentTab().renderBG(gui);
		} catch(Throwable t) { if(logError) { LightmansCurrency.LogError("Error rendering " + this.currentTab().getClass().getName() + " tab.", t); logError = false; } }

		gui.drawString(this.playerInventoryTitle, 8, this.getYSize() - 94, 0x404040);

	}

	@Override
	protected void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {
		try{ this.currentTab().renderAfterWidgets(gui);
		} catch(Throwable t) { if(logError) { LightmansCurrency.LogError("Error rendering " + this.currentTab().getClass().getName() + " tab.", t); logError = false; } }
	}

	public void changeTab(int tabIndex)
	{
		
		//Close the old tab
		this.currentTab().onClose();
		this.tabButtons.get(this.currentTabIndex).active = true;
		this.currentTabIndex = MathUtil.clamp(tabIndex, 0, this.tabs.size() - 1);
		this.tabButtons.get(this.currentTabIndex).active = false;
		
		//Initialize the new tab
		this.currentTab().onOpen();
		
		this.logError = true;
	}
	
	private void clickedOnTab(EasyButton tab)
	{
		int tabIndex = -1;
		if(tab instanceof TabButton)
			tabIndex = this.tabButtons.indexOf(tab);
		if(tabIndex < 0)
			return;
		this.changeTab(tabIndex);
	}

	@Override
	public boolean blockInventoryClosing() { return this.currentTab().blockInventoryClosing(); }
}
