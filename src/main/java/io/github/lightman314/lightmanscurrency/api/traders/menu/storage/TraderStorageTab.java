package io.github.lightman314.lightmanscurrency.api.traders.menu.storage;

import io.github.lightman314.lightmanscurrency.common.menus.tabbed.EasyMenuTab;

import javax.annotation.Nonnull;

public abstract class TraderStorageTab extends EasyMenuTab<ITraderStorageMenu,TraderStorageTab> {

	//0-9 "Basic Tabs"
	public static final int TAB_TRADE_BASIC = 0;
	public static final int TAB_TRADE_STORAGE = 1;
	public static final int TAB_TRADE_ADVANCED = 2;
	public static final int TAB_TRADE_MISC = 3;
	public static final int TAB_MONEY_STORAGE = 4;
	public static final int TAB_TRADE_MULTI_PRICE = 9;

	//10-49 "Settings and Logs"
	public static final int TAB_TRADER_INFO = 10;
	public static final int TAB_TRADER_SETTINGS = 11;

	//50 "Settings Clipboard"
	public static final int TAB_SETTINGS_CLIPBOARD = 50;

	//100 & 101 "Trade Rules"
	public static final int TAB_RULES_TRADER = 100;
	public static final int TAB_RULES_TRADE = 101;

	protected TraderStorageTab(@Nonnull ITraderStorageMenu menu) { super(menu); }
	
}
