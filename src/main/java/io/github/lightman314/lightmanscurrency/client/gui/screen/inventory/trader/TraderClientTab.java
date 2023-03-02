package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

public abstract class TraderClientTab {
	
	protected final TraderScreen screen;
	protected final TraderMenu menu;
	protected final Font font;
	
	protected TraderClientTab(TraderScreen screen) {
		this.screen = screen;
		this.menu = this.screen.getMenu();
		Minecraft mc = Minecraft.getInstance();
		this.font = mc.font;
	}
	
	/**
	 * Whether this tab being open should prevent the inventory button from closing the screen. Use this when typing is used on this tab.
	 */
	public abstract boolean blockInventoryClosing();
	
	/**
	 * Called when the tab is opened. Use this to initialize buttons/widgets and reset variables
	 */
	public abstract void onOpen();
	
	/**
	 * Called every container tick
	 */
	public void tick() { }
	
	/**
	 * Renders background data before the rendering of buttons/widgets and item slots
	 */
	public abstract void renderBG(PoseStack pose, int mouseX, int mouseY, float partialTicks);
	
	/**
	 * Renders tooltips after the rendering of buttons/widgets and item slots
	 */
	public abstract void renderTooltips(PoseStack pose, int mouseX, int mouseY);
	
	/**
	 * Called when the mouse is clicked before any other click interactions are processed.
	 * Return true an action was taken and other click interactions should be ignored.
	 */
	public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }
	
	/**
	 * Called when the mouse is clicked before any other click interactions are processed.
	 * Return true an action was taken and other click interactions should be ignored.
	 */
	public boolean mouseReleased(double mouseX, double mouseY, int button) { return false; }
	
	/**
	 * Called when the tab is closed.
	 */
	public void onClose() { }
	
}
