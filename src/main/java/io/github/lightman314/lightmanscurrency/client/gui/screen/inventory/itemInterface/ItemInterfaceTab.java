package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.itemInterface;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ItemInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TabButton.ITab;

public abstract class ItemInterfaceTab implements ITab
{
	protected final ItemInterfaceScreen screen;
	
	protected ItemInterfaceTab(ItemInterfaceScreen screen) { this.screen = screen; }
	
	public abstract void init();
	
	public abstract void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks);
	
	public abstract void postRender(PoseStack pose, int mouseX, int mouseY);
	
	public abstract void tick();
	
	public abstract void onClose();
	
	public boolean blockInventoryClosing() { return false; }
	
	public final int getColor() { return 0xFFFFFF; }
	
}
