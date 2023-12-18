package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TeamSelectWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.base.OwnershipTab;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;

public class OwnershipClientTab extends TraderInterfaceClientTab<OwnershipTab> {

	public OwnershipClientTab(TraderInterfaceScreen screen, OwnershipTab tab) { super(screen, tab); }
	
	@Nonnull
	@Override
	public IconData getIcon() { return IconData.of(Items.PLAYER_HEAD); }

	@Override
	public MutableComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.settings.owner"); }
	
	@Override
	public boolean blockInventoryClosing() { return true; }
	
	EditBox newOwnerInput;
	EasyButton buttonSetOwner;
	TeamSelectWidget teamSelection;
	EasyButton buttonSetTeamOwner;
	
	long selectedTeam = -1;
	List<Team> teamList = new ArrayList<>();
	
	IconButton buttonToggleMode;
	
	boolean playerMode = true;
	
	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {
		
		this.newOwnerInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 23, screenArea.y + 26, 160, 20, EasyText.empty()));
		this.newOwnerInput.setMaxLength(16);
		
		this.buttonSetOwner = this.addChild(new EasyTextButton(screenArea.pos.offset(23, 47), 160, 20, EasyText.translatable("gui.button.lightmanscurrency.set_owner"), this::setOwner)
				.withAddons(EasyAddonHelper.tooltip(IconAndButtonUtil.TOOLTIP_CANNOT_BE_UNDONE)));
		this.buttonSetOwner.active = false;
		
		this.teamSelection = this.addChild(new TeamSelectWidget(screenArea.pos.offset(13, 25), 4, () -> this.teamList, this::getSelectedTeam, this::selectTeam));

		this.buttonSetTeamOwner = this.addChild(new EasyTextButton(screenArea.pos.offset(23, 117), 160, 20, EasyText.translatable("gui.button.lightmanscurrency.set_owner"), this::setTeamOwner)
				.withAddons(EasyAddonHelper.tooltip(IconAndButtonUtil.TOOLTIP_CANNOT_BE_UNDONE)));
		this.buttonSetTeamOwner.active = false;
		
		this.buttonToggleMode = this.addChild(new IconButton(screenArea.pos.offset(screenArea.width - IconButton.SIZE - 3, 3), this::toggleMode, this::getModeIcon)
				.withAddons(EasyAddonHelper.toggleTooltip(() -> this.playerMode, EasyText.translatable("tooltip.lightmanscurrency.settings.owner.player"), EasyText.translatable("tooltip.lightmanscurrency.settings.owner.team"))));
		
		this.tick();
		
	}
	
	private IconData getModeIcon() { return this.playerMode ? IconData.of(Items.PLAYER_HEAD) : IconData.of(Items.WRITABLE_BOOK); }
	
	private Team getTeam(int teamIndex)
	{
		if(teamIndex < this.teamList.size())
			return this.teamList.get(teamIndex);
		return null;
	}
	
	private Team getSelectedTeam()
	{
		if(this.selectedTeam < 0)
			return null;
		return TeamSaveData.GetTeam(true, this.selectedTeam);
	}
	
	private void refreshTeamList()
	{
		this.teamList = new ArrayList<>();
		List<Team> allTeams = TeamSaveData.GetAllTeams(true);
		allTeams.forEach(team ->{
			if(team.isMember(this.menu.player))
				this.teamList.add(team);
		});
		this.teamList.sort(Team.sorterFor(this.menu.player));
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {
		
		if(this.menu.getBE() == null)
			return;

		gui.drawString(TextRenderUtil.fitString(EasyText.translatable("gui.button.lightmanscurrency.team.owner", this.menu.getBE().getOwnerName()), this.screen.getXSize() - 20), 10, 10, 0x404040);
		
	}
	
	private void toggleMode(EasyButton button) { this.playerMode = !this.playerMode; }
	
	private void setOwner(EasyButton button)
	{
		if(this.newOwnerInput.getValue().isBlank())
			return;
		this.commonTab.setNewOwner(this.newOwnerInput.getValue());
		this.newOwnerInput.setValue("");
	}
	
	private void setTeamOwner(EasyButton button) {
		if(this.selectedTeam < 0)
			return;
		this.commonTab.setNewTeam(this.selectedTeam);
		this.selectedTeam = -1;
	}
	
	private void selectTeam(int teamIndex)
	{
		Team newTeam = this.getTeam(teamIndex);
		if(newTeam != null)
		{
			if(newTeam.getID() == this.selectedTeam)
				this.selectedTeam = -1;
			else
				this.selectedTeam = newTeam.getID();
		}
	}
	
	@Override
	public void tick() {
		this.refreshTeamList();
		
		this.buttonSetOwner.visible = this.newOwnerInput.visible = this.playerMode;
		this.buttonSetTeamOwner.visible = this.teamSelection.visible = !this.playerMode;
		
		this.buttonSetOwner.active = !this.newOwnerInput.getValue().isBlank();
		this.buttonSetTeamOwner.active = this.getSelectedTeam() != null;
	}

	@Override
	public void closeAction() {
		//Reset the selected team & team list to saveItem space
		this.selectedTeam = -1;
		this.teamList = new ArrayList<>();
	}

}
