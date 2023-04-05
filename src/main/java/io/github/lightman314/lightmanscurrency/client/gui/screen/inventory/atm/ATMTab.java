package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TabButton.ITab;
import net.minecraft.client.gui.GuiComponent;

public abstract class ATMTab implements ITab
{
	protected final ATMScreen screen;
	
	protected ATMTab(ATMScreen screen) { this.screen = screen; }
	
	public abstract void init();
	
	public abstract void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks);
	
	public abstract void postRender(PoseStack pose, int mouseX, int mouseY);
	
	public abstract void tick();
	
	public abstract void onClose();
	
	public boolean blockInventoryClosing() { return false; }
	
	public final int getColor() { return 0xFFFFFF; }
	
	protected final void hideCoinSlots(PoseStack pose) {
		RenderSystem.setShaderTexture(0, ATMScreen.GUI_TEXTURE);
		GuiComponent.blit(pose, this.screen.getGuiLeft() + 7, this.screen.getGuiTop() + 128, 7, 79, 162, 18);
	}
	
}
