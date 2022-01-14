package io.github.lightman314.lightmanscurrency.client.gui.team;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TabButton.ITab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public abstract class TeamTab implements ITab{

	public int getColor() { return 0xFFFFFF; }
	public abstract IconData getIcon();
	public abstract Component getTooltip();
	
	private TeamManagerScreen screen;
	protected final TeamManagerScreen getScreen() { return this.screen; }
	protected final Player getPlayer() { return this.screen.getPlayer(); }
	protected final Font getFont() { return this.screen.getFont(); }
	protected final Team getActiveTeam() { return this.screen.getActiveTeam(); }
	public final void setScreen(TeamManagerScreen screen) { this.screen = screen; }
	
	/**
	 * Returns whether a player is allowed to view this tab.
	 */
	public abstract boolean allowViewing(Player player, Team team);
	
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
