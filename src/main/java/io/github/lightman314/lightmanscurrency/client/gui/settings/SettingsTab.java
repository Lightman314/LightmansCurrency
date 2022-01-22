package io.github.lightman314.lightmanscurrency.client.gui.settings;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TabButton.ITab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public abstract class SettingsTab implements ITab{

	public abstract int getColor();
	public abstract IconData getIcon();
	public abstract Component getTooltip();
	
	private TraderSettingsScreen screen;
	protected final TraderSettingsScreen getScreen() { return this.screen; }
	protected final Player getPlayer() { return this.screen.getPlayer(); }
	protected final Font getFont() { return this.screen.getFont(); }
	protected final <T extends Settings> T getSetting(Class<T> type) { return this.screen.getSetting(type); }
	public final void setScreen(TraderSettingsScreen screen) { this.screen = screen; }
	
	public boolean canOpen()
	{
		return this.getScreen().hasPermissions(this.requiredPermissions());
	}
	
	/**
	 * Returns a list of required permissions that the player must have in order to see this tab.
	 */
	@Deprecated //Use canOpen instead
	public abstract ImmutableList<String> requiredPermissions();
	
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