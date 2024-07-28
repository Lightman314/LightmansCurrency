package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyTab;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.common.menus.ATMMenu;

public abstract class ATMTab extends EasyTab
{
	protected final ATMScreen screen;
	protected final ATMMenu menu;
	
	protected ATMTab(ATMScreen screen) { super(screen); this.screen = screen; this.menu = this.screen.getMenu(); }
	
	protected final void hideCoinSlots(EasyGuiGraphics gui) {
		gui.blit(ATMScreen.GUI_TEXTURE, 7, 128, 7, 79, 162, 18);
	}
	
}
