package io.github.lightman314.lightmanscurrency.client.gui.screen.team;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TeamSelectWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.network.message.teams.CPacketCreateTeam;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class TeamSelectionTab extends TeamTab {
	
	public TeamSelectionTab(TeamManagerScreen screen) { super(screen); }
	
	@Nonnull
    @Override
	public @NotNull IconData getIcon() { return IconData.of(Items.PAPER); }

	@Override
	public MutableComponent getTooltip() { return Component.translatable("tooltip.lightmanscurrency.team.selection"); }

	@Override
	public boolean allowViewing(Player player, Team team) { return true; }

	TeamSelectWidget teamSelection;
	List<Team> teamList = Lists.newArrayList();
	
	EditBox newTeamName;
	EasyButton buttonCreateTeam;

	@Override
	public boolean blockInventoryClosing() { return true; }

	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {
		
		this.refreshTeamList();
		
		this.teamSelection = this.addChild(new TeamSelectWidget(screenArea.pos.offset(10, 20), 5, () -> this.teamList, this::getActiveTeam, this::selectTeamButton));
		
		this.newTeamName = this.addChild(new EditBox(this.getFont(), screenArea.x + 20, screenArea.y + 140, 160, 20, Component.empty()));
		this.newTeamName.setMaxLength(32);
		
		this.buttonCreateTeam = this.addChild(new EasyTextButton(screenArea.pos.offset(120, 165), 60, 20, EasyText.translatable("gui.button.lightmanscurrency.team.create"), this::createTeam));
		this.buttonCreateTeam.active = false;
		
	}
	
	private Team getTeam(int teamIndex)
	{
		if(teamIndex < this.teamList.size())
			return this.teamList.get(teamIndex);
		return null;
	}
	
	private void refreshTeamList()
	{
		this.teamList = Lists.newArrayList();
		List<Team> allTeams = TeamSaveData.GetAllTeams(true);
		allTeams.forEach(team ->{
			if(team.isMember(this.getPlayer()))
				this.teamList.add(team);
		});
		this.teamList.sort(Team.sorterFor(this.getPlayer()));
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {
		
		//Render the text
		gui.drawString(EasyText.translatable("gui.lightmanscurrency.team.select"), 20, 10, 0x404040);
		gui.drawString(EasyText.translatable("gui.lightmanscurrency.team.create"), 20, 130, 0x404040);
		
	}

	@Override
	public void tick() {
		
		//Refresh the team list
		this.refreshTeamList();
		
		this.buttonCreateTeam.active = !this.newTeamName.getValue().isBlank();
		
	}
	
	private void selectTeamButton(int teamIndex)
	{
		Team team = this.getTeam(teamIndex);
		if(team != null)
		{
			if(this.screen.getActiveTeam() == team)
				this.screen.setActiveTeam(-1);
			else
				this.screen.setActiveTeam(team.getID());
		}
	}
	
	private void createTeam(EasyButton button)
	{
		if(this.newTeamName.getValue().isEmpty())
			return;
		new CPacketCreateTeam(this.newTeamName.getValue()).send();
		this.newTeamName.setValue("");
	}
	
}
