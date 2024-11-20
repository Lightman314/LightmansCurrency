package io.github.lightman314.lightmanscurrency.api.trader_interface.menu;

import io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed.EasyMenuClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import net.minecraft.nbt.CompoundTag;

public abstract class TraderInterfaceClientTab<T extends TraderInterfaceTab> extends EasyMenuClientTab<T,TraderInterfaceMenu,TraderInterfaceTab,TraderInterfaceScreen,TraderInterfaceClientTab<T>> {

	public TraderInterfaceClientTab(Object screen, T commonTab) { super(screen,commonTab); }

	/**
	 * Processes a client -> client message from another tab immediately after the tab was changed.
	 */
	public void receiveSelfMessage(CompoundTag message) { }

}