package io.github.lightman314.lightmanscurrency.client.gui.screen.team;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TeamSelectWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TeamButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.teams.TeamManagementClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.teams.tabs.TeamSelectionTab;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TeamSelectionClientTab extends TeamManagementClientTab<TeamSelectionTab> {

    public TeamSelectionClientTab(@Nonnull Object screen, @Nonnull TeamSelectionTab commonTab) { super(screen, commonTab); }

    TeamSelectWidget teamSelection;
    List<ITeam> teamList = new ArrayList<>();

    EditBox newTeamName;
    EasyButton buttonCreateTeam;

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.of(Items.PAPER); }

    @Override
    public MutableComponent getTooltip() { return LCText.TOOLTIP_TEAM_SELECT.get(); }

    @Override
    public boolean blockInventoryClosing() { return true; }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.refreshTeamList();

        this.teamSelection = this.addChild(TeamSelectWidget.builder()
                .position(screenArea.pos.offset(10,20))
                .size(TeamButton.Size.WIDE)
                .rows(5)
                .teams(() -> this.teamList)
                .selected(this.menu::selectedTeam)
                .handler(this::selectTeamButton)
                .build());

        this.newTeamName = this.addChild(new EditBox(this.getFont(), screenArea.x + 20, screenArea.y + 140, 160, 20, Component.empty()));
        this.newTeamName.setMaxLength(32);

        this.buttonCreateTeam = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(120,165))
                .width(60)
                .text(LCText.BUTTON_TEAM_CREATE)
                .pressAction(this::createTeam)
                .build());
        this.buttonCreateTeam.active = false;

    }

    private ITeam getTeam(int teamIndex)
    {
        if(teamIndex < this.teamList.size())
            return this.teamList.get(teamIndex);
        return null;
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

        //Render the text
        gui.drawString(LCText.GUI_TEAM_SELECT.get(), 20, 10, 0x404040);
        gui.drawString(LCText.GUI_TEAM_CREATE.get(), 20, 130, 0x404040);

    }

    @Override
    public void tick() {

        //Refresh the team list
        this.refreshTeamList();

        this.buttonCreateTeam.active = !this.newTeamName.getValue().isBlank();

    }

    private void selectTeamButton(int teamIndex)
    {
        ITeam team = this.getTeam(teamIndex);
        if(team != null)
            this.commonTab.SelectTeam(team.getID());
    }

    private void createTeam(EasyButton button)
    {
        if(this.newTeamName.getValue().isBlank())
            return;
        this.commonTab.CreateTeam(this.newTeamName.getValue());
        this.newTeamName.setValue("");
    }

}