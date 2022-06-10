package io.github.lightman314.lightmanscurrency.client.gui.settings;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TabButton.ITab;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import net.minecraft.client.gui.Font;
import net.minecraft.world.entity.player.Player;

public abstract class SettingsTab implements ITab{

	/* Obsolete, as these are included in ITab
	public abstract int getColor();
	public abstract IconData getIcon();
	public abstract MutableComponent getTooltip();
	*/
	
	private TraderSettingsScreen screen;
	protected final TraderSettingsScreen getScreen() { return this.screen; }
	protected final Player getPlayer() { return this.screen.getPlayer(); }
	protected final Font getFont() { return this.screen.getFont(); }
	protected final <T extends Settings> T getSetting(Class<T> type) { return this.screen.getSetting(type); }
	public final void setScreen(TraderSettingsScreen screen) { this.screen = screen; }
	
	public abstract boolean canOpen();
	
	protected final boolean hasPermissions(String... permissions) {
		for(String perm : permissions) {
			if(!this.screen.hasPermission(perm))
				return false;
		}
		return true;
	}
	
	/**
	 * Called when the tab is opened.
	 * Used to initialize any widgets being used, or other relevant local variables.
	 */
	public abstract void initTab();
	
	/**
	 * Called when the tab is being rendered.
	 * Used to render any text, images, etc. Called before the buttons are rendered, so you don't have to worry about accidentally drawing over them.
	 */
	public abstract void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks);
	
	/**
	 * Called when the tab is being rendered.
	 * Used to render any tooltips, etc. Called after the buttons are rendered so that tooltips will appear in front.
	 */
	public abstract void postRender(PoseStack pose, int mouseX, int mouseY, float partialTicks);
	
	/**
	 * Called every frame.
	 * Used to re-determine if certain widgets should still be visible, etc.
	 */
	public abstract void tick();
	
	/**
	 * Called when the tab is changed to another tab.
	 * Used to remove any widgets that were added.
	 * @param screen
	 */
	public abstract void closeTab();
	
}
