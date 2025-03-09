package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed.EasyClientTabbedMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm.*;
import io.github.lightman314.lightmanscurrency.client.gui.util.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.WidgetRotation;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.menus.ATMMenu;
import io.github.lightman314.lightmanscurrency.common.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasySlot;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class ATMScreen extends EasyClientTabbedMenuScreen<ATMMenu,ATMScreen,ATMTab> {

	public static final ResourceLocation GUI_TEXTURE = VersionUtil.lcResource("textures/gui/container/atm.png");

	public static final ResourceLocation BUTTON_TEXTURE = VersionUtil.lcResource("textures/gui/container/atm_buttons.png");

	public ATMScreen(ATMMenu container, Inventory inventory, Component title) {
		super(container, inventory, title);
		this.resize(176, 243);
		this.initializeTabs();
	}

	@Nonnull
	@Override
	protected IWidgetPositioner getTabButtonPositioner() { return LazyWidgetPositioner.create(this,LazyWidgetPositioner.createTopdown(WidgetRotation.LEFT), ScreenPosition.of(TabButton.NEGATIVE_SIZE,0),TabButton.SIZE); }

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
	}

	@Override
	protected void renderBackground(@Nonnull EasyGuiGraphics gui) {

		gui.renderNormalBackground(GUI_TEXTURE, this);
		//Render Coin Slots if they're active
		for(CoinSlot slot : this.menu.getCoinSlots())
			gui.renderSlot(this,slot);

		gui.drawString(this.playerInventoryTitle, 8, this.getYSize() - 94, 0x404040);

	}

	public void setCoinSlotsActive(boolean active) {
		EasySlot.SetActive(this.menu.getCoinSlots(),active);
	}

}