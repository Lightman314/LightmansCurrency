package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyTab;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;

public abstract class ATMTab extends EasyTab
{
	protected final ATMScreen screen;
	
	protected ATMTab(ATMScreen screen) { super(screen); this.screen = screen; }
	
	public final int getColor() { return 0xFFFFFF; }
	
	protected final void hideCoinSlots(EasyGuiGraphics gui) {
		gui.blit(ATMScreen.GUI_TEXTURE, 7, 128, 7, 79, 162, 18);
	}
	
}
