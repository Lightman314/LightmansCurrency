package io.github.lightman314.lightmanscurrency.common.menus.traderinterface;

import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import net.minecraft.nbt.CompoundTag;

public abstract class TraderInterfaceClientTab<T extends TraderInterfaceTab> extends EasyTab {

	protected final TraderInterfaceScreen screen;
	protected final TraderInterfaceMenu menu;
	public final T commonTab;
	
	protected TraderInterfaceClientTab(TraderInterfaceScreen screen, T commonTab) {
		super(screen);
		this.screen = screen;
		this.menu = this.screen.getMenu();
		this.commonTab = commonTab;
	}
	
	@Override
	public int getColor() { return 0xFFFFFF; }
	
	/**
	 * Whether the tab button for this tab should be visible. Used to hide the advanced trade tab from the screen, to only be opened when needed.
	 */
	public boolean tabButtonVisible() { return this.commonTab.canOpen(this.menu.player); }
	
	/**
	 * Processes a client -> client message from another tab immediately after the tab was changed.
	 */
	public void receiveSelfMessage(CompoundTag message) { }
	

}
