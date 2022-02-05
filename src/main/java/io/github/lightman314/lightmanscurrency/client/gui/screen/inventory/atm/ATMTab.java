package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TabButton.ITab;

public abstract class ATMTab implements ITab
{
	protected final ATMScreen screen;
	
	protected ATMTab(ATMScreen screen) { this.screen = screen; }
	
	public abstract void init();
	
	public abstract void preRender(MatrixStack matrix, int mouseX, int mouseY, float partialTicks);
	
	public abstract void postRender(MatrixStack matrix, int mouseX, int mouseY);
	
	public abstract void tick();
	
	public abstract void onClose();
	
	public final int getColor() { return 0xFFFFFF; }
	
}
