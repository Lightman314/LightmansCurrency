package io.github.lightman314.lightmanscurrency.client.gui.screen.team;

import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import net.minecraft.world.entity.player.Player;

public abstract class TeamTab extends EasyTab {


	public int getColor() { return 0xFFFFFF; }
	
	protected final TeamManagerScreen screen;
	protected final Player getPlayer() { return this.screen.getPlayer(); }
	protected final Team getActiveTeam() { return this.screen.getActiveTeam(); }

	protected TeamTab(TeamManagerScreen screen) { super(screen); this.screen = screen; }
	
	/**
	 * Returns whether a player is allowed to view this tab.
	 */
	public abstract boolean allowViewing(Player player, Team team);
	
}
