package io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.client.gui.tabbed.EasyMenuClientTab;
import io.github.lightman314.lightmanscurrency.api.client.gui.tabbed.ISortableTab;

public abstract class TraderStorageClientTab<T extends TraderStorageTab> extends EasyMenuClientTab<T, ITraderStorageMenu,TraderStorageTab,ITraderStorageScreen,TraderStorageClientTab<T>> implements ISortableTab {

	protected TraderStorageClientTab(Object screen, T commonTab) { super(screen,commonTab); }

	/**
	 * The trade index of the trade that the trade rule button should open.
	 */
	public int getTradeRuleTradeIndex() { return -1; }
	
	/**
	 * Processes a server -> client message response to an action made on the client.
	 */
	public void receiveServerMessage(LazyPacketData message) { }

	public boolean shouldRenderInventoryText() { return true; }

	public boolean showRightEdgeButtons() { return true; }

    @Override
    public int getSortPriority() { return this.commonTab.getSortPriority(); }

}
