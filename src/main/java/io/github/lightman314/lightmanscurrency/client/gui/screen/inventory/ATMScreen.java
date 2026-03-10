package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.client.gui.tabbed.EasyClientTabbedMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm.*;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm.salary.SalaryTab;
import io.github.lightman314.lightmanscurrency.api.client.widgets.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.WidgetRotation;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.menus.ATMMenu;
import io.github.lightman314.lightmanscurrency.api.misc.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.api.misc.menus.slots.EasySlot;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ATMScreen extends EasyClientTabbedMenuScreen<ATMMenu,ATMScreen,ATMTab> {

	public static final ResourceLocation GUI_TEXTURE = LightmansCurrency.id("textures/gui/container/atm.png");
	
	public ATMScreen(ATMMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.resize(176, 243);
		this.initializeTabs();
        this.menu.addMessageListener(message -> this.currentTab().HandleMessage(message));
	}

	@Override
	protected IWidgetPositioner getTabButtonPositioner() { return LazyWidgetPositioner.create(this,LazyWidgetPositioner.createTopdown(WidgetRotation.LEFT), ScreenPosition.of(TabButton.NEGATIVE_SIZE,0), TabButton.SIZE); }

	@Override
	protected void init(ScreenArea screenArea) { }

	@Override
	protected void registerTabs() {
		this.addTab(new ExchangeTab(this));
		this.addTab(new SelectionTab(this));
		this.addTab(new InteractionTab(this));
		this.addTab(new NotificationTab(this));
		this.addTab(new LogTab(this));
		this.addTab(new TransferTab(this));
        this.addTab(new SalaryTab(this));
	}

	@Override
	protected void renderBackground(EasyGuiGraphics gui) {

		gui.renderNormalBackground(GUI_TEXTURE, this);
		//Render Coin Slots if they're active
		for(CoinSlot slot : this.menu.getCoinSlots())
			gui.renderSlot(this,slot);

        //Render Inventory Label
        if(this.currentTab().renderInventoryLabel())
		    gui.drawString(this.playerInventoryTitle, 8, this.getYSize() - 94, 0x404040);

	}

	public void setCoinSlotsActive(boolean active) { EasySlot.SetActive(this.menu.getCoinSlots(),active); }

}
