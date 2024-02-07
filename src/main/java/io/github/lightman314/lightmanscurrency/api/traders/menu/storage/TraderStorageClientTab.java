package io.github.lightman314.lightmanscurrency.api.traders.menu.storage;

import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyTab;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IEasyScreen;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class TraderStorageClientTab<T extends TraderStorageTab> extends EasyTab {

	public final ITraderStorageScreen screen;
	public final ITraderStorageMenu menu;
	public final T commonTab;
	
	protected TraderStorageClientTab(Object screen, T commonTab) {
		super((IEasyScreen)screen);
		this.screen = (ITraderStorageScreen)screen;
		this.menu = this.screen.getMenu();
		this.commonTab = commonTab;
	}
	
	@Override
	public int getColor() { return 0xFFFFFF; }
	
	/**
	 * Whether the tab button for this tab should be visible. Used to hide the advanced trade tab from the screen, to only be opened when needed.
	 */
	public boolean tabButtonVisible() { return true; }
	
	/**
	 * The trade index of the trade that the trade rule button should open.
	 */
	public int getTradeRuleTradeIndex() { return -1; }
	
	/**
	 * Processes a client -> client message from another tab immediately after the tab was changed.
	 */
	public void receiveSelfMessage(LazyPacketData message) { }
	
	/**
	 * Processes a server -> client message response to an action made on the client.
	 */
	public void receiveServerMessage(LazyPacketData message) { }

	public boolean shouldRenderInventoryText() { return true; }

}
