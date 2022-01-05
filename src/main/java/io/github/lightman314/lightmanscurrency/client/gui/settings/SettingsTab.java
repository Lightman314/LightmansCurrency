package io.github.lightman314.lightmanscurrency.client.gui.settings;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;

public abstract class SettingsTab {

	public abstract int getColor();
	public abstract IconData getIcon();
	public abstract ITextComponent getTooltip();
	
	private TraderSettingsScreen screen;
	protected final TraderSettingsScreen getScreen() { return this.screen; }
	protected final PlayerEntity getPlayer() { return this.screen.getPlayer(); }
	protected final FontRenderer getFont() { return this.screen.getFont(); }
	protected final <T extends Settings> T getSetting(Class<T> type) { return this.screen.getSetting(type); }
	public final void setScreen(TraderSettingsScreen screen) { this.screen = screen; }
	
	/**
	 * Returns a list of required permissions that the player must have in order to see this tab.
	 */
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
	public abstract void preRender(MatrixStack matrix, int mouseX, int mouseY, float partialTicks);
	
	/**
	 * Called when the tab is being rendered.
	 * Used to render any tooltips, etc. Called after the buttons are rendered so that tooltips will appear in front.
	 */
	public abstract void postRender(MatrixStack matrix, int mouseX, int mouseY, float partialTicks);
	
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
