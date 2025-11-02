package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed.EasyClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.common.menus.ATMMenu;

public abstract class ATMTab extends EasyClientTab<ATMMenu,ATMScreen,ATMTab>
{
	protected final ATMScreen screen;
	protected final ATMMenu menu;

	public ATMTab(ATMScreen screen) { super(screen); this.screen = screen; this.menu = this.screen.getMenu(); }

    public void HandleMessage(LazyPacketData message) { }

    public boolean renderInventoryLabel() { return true; }

}