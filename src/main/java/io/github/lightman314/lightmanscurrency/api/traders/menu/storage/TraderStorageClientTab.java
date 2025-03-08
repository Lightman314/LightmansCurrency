package io.github.lightman314.lightmanscurrency.api.traders.menu.storage;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed.EasyMenuClientTab;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class TraderStorageClientTab<T extends TraderStorageTab> extends EasyMenuClientTab<T,ITraderStorageMenu,TraderStorageTab,ITraderStorageScreen,TraderStorageClientTab<T>> {

	
	protected TraderStorageClientTab(Object screen, T commonTab) {
		super(screen,commonTab);
	}

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

}
