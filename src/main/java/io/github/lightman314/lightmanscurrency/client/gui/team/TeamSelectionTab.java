package io.github.lightman314.lightmanscurrency.client.gui.team;

import java.util.List;

import com.google.common.collect.Lists;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TeamSelectWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.teams.MessageCreateTeam;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

public class TeamSelectionTab extends TeamTab {
	
	public static final TeamSelectionTab INSTANCE = new TeamSelectionTab();
	
	private TeamSelectionTab() { }
	
	@Nonnull
    @Override
	public IconData getIcon() { return IconData.of(Items.PAPER); }

	@Override
	public ITextComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.team.selection"); }

	@Override
	public boolean allowViewing(PlayerEntity player, Team team) { return true; }

	TeamSelectWidget teamSelection;
	List<Team> teamList = Lists.newArrayList();
	
	TextFieldWidget newTeamName;
	Button buttonCreateTeam;
	
	@Override
	public void initTab() {
		
		TeamManagerScreen screen = this.getScreen();
		
		this.refreshTeamList();
		
		this.teamSelection = screen.addRenderableTabWidget(new TeamSelectWidget(screen.guiLeft() + 10, screen.guiTop() + 20, 5, () -> this.teamList, this::getActiveTeam, this::selectTeamButton));
		this.teamSelection.init(screen::addRenderableTabWidget, this.getFont());
		
		this.newTeamName = screen.addRenderableTabWidget(new TextFieldWidget(this.getFont(), screen.guiLeft() + 20, screen.guiTop() + 140, 160, 20, EasyText.empty()));
		this.newTeamName.setMaxLength(32);
		
		this.buttonCreateTeam = screen.addRenderableTabWidget(new Button(screen.guiLeft() + 120, screen.guiTop() + 165, 60, 20, EasyText.translatable("gui.button.lightmanscurrency.team.create"), this::createTeam));
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
	public void preRender(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		
		TeamManagerScreen screen = this.getScreen();
		
		//Render the text
		this.getFont().draw(pose, EasyText.translatable("gui.lightmanscurrency.team.select"), screen.guiLeft() + 20, screen.guiTop() + 10, 0x404040);
		this.getFont().draw(pose, EasyText.translatable("gui.lightmanscurrency.team.create"), screen.guiLeft() + 20, screen.guiTop() + 130, 0x404040);
		
	}

	@Override
	public void postRender(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		
	}

	@Override
	public void tick() {
		
		//Refresh the team list
		this.refreshTeamList();
		
		this.buttonCreateTeam.active = !this.newTeamName.getValue().isEmpty();
		
	}

	@Override
	public void closeTab() { }
	
	private void selectTeamButton(int teamIndex)
	{
		Team team = this.getTeam(teamIndex);
		if(team != null)
		{
			if(this.getScreen().getActiveTeam() == team)
				this.getScreen().setActiveTeam(-1);
			else
				this.getScreen().setActiveTeam(team.getID());
		}
	}
	
	private void createTeam(Button button)
	{
		if(this.newTeamName.getValue().isEmpty())
			return;
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCreateTeam(this.newTeamName.getValue()));
		this.newTeamName.setValue("");
	}
	
}