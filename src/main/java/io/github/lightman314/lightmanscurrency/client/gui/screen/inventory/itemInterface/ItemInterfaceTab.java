package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.itemInterface;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.blockentity.UniversalTraderInterfaceBlockEntity.InteractionType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ItemInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TabButton.ITab;

public abstract class ItemInterfaceTab implements ITab
{
	protected final ItemInterfaceScreen screen;
	public final boolean hideItemSlots;
	
	protected ItemInterfaceTab(ItemInterfaceScreen screen, boolean hideItemSlots) { this.screen = screen; this.hideItemSlots = hideItemSlots; }
	
	public abstract boolean valid(InteractionType interaction);
	
	public abstract void init();
	
	public abstract void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks);
	
	public abstract void postRender(PoseStack pose, int mouseX, int mouseY);
	
	public abstract void tick();
	
	public abstract void onClose();
	
	public boolean blockInventoryClosing() { return false; }
	
	public final int getColor() { return 0xFFFFFF; }
	
	public boolean charTyped(char c, int code) { return false; }
	
	public boolean keyPressed(int key, int scanCode, int mods) { return false; }
	
}
