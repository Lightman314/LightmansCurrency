package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader;

import io.github.lightman314.lightmanscurrency.api.traders.menu.customer.ITraderMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.customer.ITraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed.EasyClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.Font;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class TraderClientTab extends EasyClientTab.Unenforced<TraderMenu,TraderScreen,TraderClientTab> {
	
	protected final ITraderScreen screen;
	protected final ITraderMenu menu;
	protected final Font font;

	protected TraderClientTab(ITraderScreen screen) {
		super((TraderScreen)screen);
		this.screen = screen;
		this.menu = this.screen.getMenu();
		this.font = this.screen.getFont();
	}
	
}
