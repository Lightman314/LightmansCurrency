package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.tax_collector;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TeamSelectWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TeamButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.TaxCollectorClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.tabs.OwnershipTab;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class OwnershipClientTab extends TaxCollectorClientTab<OwnershipTab> {

    public OwnershipClientTab(Object screen, OwnershipTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.of(Items.PLAYER_HEAD); }

    @Nullable
    @Override
    public Component getTooltip() { return EasyText.translatable("gui.lightmanscurrency.tax_collector.owner"); }

    private boolean playerMode = true;
    private boolean isPlayerMode() { return this.playerMode; }
    private boolean isTeamMode() { return !this.playerMode; }

    private EditBox newOwnerInput;

    private long selectedTeam = -1;
    private List<Team> teamList = new ArrayList<>();

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        if(firstOpen)
        {
            this.selectedTeam = -1;
            this.playerMode = true;
        }

        this.newOwnerInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 10, screenArea.y + 50, screenArea.width - 20, 20, EasyText.empty()));
        this.newOwnerInput.setMaxLength(16);

        this.addChild(new EasyTextButton(screenArea.pos.offset(10, 72), screenArea.width - 20, 20, EasyText.translatable("gui.button.lightmanscurrency.set_owner"), this::SetOwnerPlayer)
                .withAddons(EasyAddonHelper.visibleCheck(this::isPlayerMode),
                        EasyAddonHelper.activeCheck(() -> this.newOwnerInput != null && !this.newOwnerInput.getValue().isBlank()),
                        EasyAddonHelper.tooltip(IconAndButtonUtil.TOOLTIP_CANNOT_BE_UNDONE)));

        this.addChild(new TeamSelectWidget(screenArea.pos.offset(10, 40), 5, TeamButton.Size.NORMAL, () -> this.teamList, this::getSelectedTeam, this::selectTeam)
                .withAddons(EasyAddonHelper.visibleCheck(this::isTeamMode)));

        this.addChild(new EasyTextButton(screenArea.pos.offset(10, 145), screenArea.width - 20, 20, EasyText.translatable("gui.button.lightmanscurrency.set_owner"), this::SetOwnerTeam)
                .withAddons(EasyAddonHelper.visibleCheck(this::isTeamMode),
                        EasyAddonHelper.activeCheck(() -> this.getSelectedTeam() != null),
                        EasyAddonHelper.tooltip(IconAndButtonUtil.TOOLTIP_CANNOT_BE_UNDONE)));

        this.addChild(new IconButton(screenArea.pos.offset(screenArea.width - 25, 5), this::ToggleMode, this::GetModeIcon)
                .withAddons(EasyAddonHelper.toggleTooltip(this::isPlayerMode, EasyText.translatable("tooltip.lightmanscurrency.settings.owner.player"), EasyText.translatable("tooltip.lightmanscurrency.settings.owner.team"))));

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        TaxEntry entry = this.getEntry();
        String ownerName = "NULL";
        if(entry != null)
            ownerName = entry.getOwner().getOwnerName();

        gui.drawString(EasyText.translatable("gui.button.lightmanscurrency.team.owner", ownerName), 8, 6, 0x404040);

    }

    @Override
    public void tick() {
        this.newOwnerInput.visible = this.isPlayerMode();
        this.recollectTeams();
    }

    private void ToggleMode(EasyButton button) { this.playerMode = !this.playerMode; }

    private IconData GetModeIcon() { return this.playerMode ? IconData.of(Items.PLAYER_HEAD) : IconData.of(Items.WRITABLE_BOOK); }

    private void SetOwnerPlayer()
    {
        if(this.newOwnerInput != null)
        {
            this.commonTab.SetOwnerPlayer(this.newOwnerInput.getValue());
            this.newOwnerInput.setValue("");
        }
    }

    private void recollectTeams()
    {
        this.teamList = new ArrayList<>();
        List<Team> allTeams = TeamSaveData.GetAllTeams(true);
        allTeams.forEach(team ->{
            if(team.isMember(this.menu.player))
                this.teamList.add(team);
        });
        this.teamList.sort(Team.sorterFor(this.menu.player));
    }

    private Team getSelectedTeam()
    {
        if(this.selectedTeam < 0)
            return null;
        return TeamSaveData.GetTeam(true, this.selectedTeam);
    }

    private void selectTeam(int teamIndex)
    {
        if(teamIndex < 0 || teamIndex >= this.teamList.size())
            return;
        Team team = this.teamList.get(teamIndex);
        if(team != null)
            this.selectedTeam = team.getID();
    }

    private void SetOwnerTeam()
    {
        if(this.selectedTeam < 0)
            return;
        this.commonTab.SetOwnerTeam(this.selectedTeam);
    }

    @Override
    protected void closeAction() {
        this.selectedTeam = -1;
        this.teamList = new ArrayList<>();
    }

    @Override
    public boolean blockInventoryClosing() { return this.newOwnerInput.isFocused(); }

}
