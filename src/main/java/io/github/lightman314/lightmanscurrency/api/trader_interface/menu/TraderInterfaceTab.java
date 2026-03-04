package io.github.lightman314.lightmanscurrency.api.trader_interface.menu;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.common.menus.tabbed.EasyMenuTab;

public abstract class TraderInterfaceTab extends EasyMenuTab<TraderInterfaceMenu,TraderInterfaceTab> {

	public static final int TAB_INFO = 0;
	public static final int TAB_STORAGE = 1;
	public static final int TAB_TRADER_SELECT = 2;
	public static final int TAB_TRADE_SELECT = 3;
	public static final int TAB_STATS = 12;
	public static final int TAB_OWNERSHIP = 100;
	
	protected TraderInterfaceTab(TraderInterfaceMenu menu) { super(menu); }

	public abstract void handleMessage(LazyPacketData message);

	@Override
	public final void receiveMessage(LazyPacketData message) { this.handleMessage(message); }

}
