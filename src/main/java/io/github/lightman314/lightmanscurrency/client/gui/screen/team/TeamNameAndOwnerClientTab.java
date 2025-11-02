package io.github.lightman314.lightmanscurrency.client.gui.screen.team;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.player.PlayerListWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.menus.teams.TeamManagementClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.teams.tabs.TeamNameAndOwnerTab;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TeamNameAndOwnerClientTab extends TeamManagementClientTab<TeamNameAndOwnerTab> {

    public TeamNameAndOwnerClientTab(@Nonnull Object screen, @Nonnull TeamNameAndOwnerTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconUtil.ICON_SHOW_LOGGER; }

    @Override
    public MutableComponent getTooltip() { return this.isOwnerAccess() ? LCText.TOOLTIP_TEAM_NAME_AND_OWNER.get() : LCText.TOOLTIP_TEAM_NAME.get(); }

    EditBox nameInput;
    EasyButton buttonChangeName;

    PlayerListWidget ownerEditWidget;

    EasyButton buttonDisbandTeam;

    @Override
    public boolean blockInventoryClosing() { return true; }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.nameInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 20, screenArea.y + 20, 160, 20, EasyText.empty()));
        this.nameInput.setMaxLength(Team.MAX_NAME_LENGTH);
        this.nameInput.setValue(this.safeGetName());

        this.buttonChangeName = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(20,45))
                .width(160)
                .text(LCText.BUTTON_TEAM_RENAME)
                .pressAction(this::changeName)
                .addon(EasyAddonHelper.activeCheck(() -> !this.safeGetName().equals(this.nameInput.getValue()) && !this.nameInput.getValue().isBlank()))
                .build());

        this.ownerEditWidget = this.addChild(PlayerListWidget.builder()
                .position(screenArea.pos.offset(20,90))
                .width(screenArea.width - 40)
                .rows(1)
                .addPlayer(this.commonTab::SetOwner)
                .canAddPlayer(this::canSetOwner)
                .addPlayerTooltip(LCText.BUTTON_OWNER_SET_PLAYER)
                .playerList(this::getOwner)
                .addon(EasyAddonHelper.visibleCheck(this::isOwnerAccess))
                .build());

        this.buttonDisbandTeam = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(20,160))
                .width(160)
                .text(LCText.BUTTON_TEAM_DISBAND)
                .pressAction(this::disbandTeam)
                .addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_WARNING_CANT_BE_UNDONE.getWithStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW)))
                .addon(EasyAddonHelper.visibleCheck(this::isOwnerAccess))
                .build());

        this.tick();

    }

    private boolean isOwnerAccess() {
        ITeam team = this.menu.selectedTeam();
        return team != null && team.isOwner(this.menu.player);
    }

    private String safeGetName() { return this.menu.selectedTeam() == null ? "NULL" : this.menu.selectedTeam().getName(); }


    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        ITeam team = this.menu.selectedTeam();
        if(team == null)
            return;

        gui.drawString(TextRenderUtil.fitString(LCText.GUI_TEAM_NAME_CURRENT.get(team.getName()),this.screen.getXSize() - 30), 20, 10, 0x404040);

        if(team.isOwner(this.menu.player))
        {
            gui.drawString(LCText.GUI_OWNER_CURRENT.get(team.getOwner().getName(true)), 20, 80, 0x404040);

            TextRenderUtil.drawCenteredText(gui, LCText.GUI_TEAM_ID.get(team.getID()), this.screen.getXSize() / 2, 184, 0x404040);
        }

    }

    private void changeName(EasyButton button)
    {
        if(this.nameInput.getValue().isBlank())
            return;

        this.commonTab.ChangeName(this.nameInput.getValue());
    }

    private List<PlayerReference> getOwner() {
        ITeam team = this.menu.selectedTeam();
        if(team != null)
            return Lists.newArrayList(team.getOwner());
        return new ArrayList<>();
    }

    private boolean canSetOwner(PlayerReference player) {
        ITeam team = this.menu.selectedTeam();
        return team != null && !team.getOwner().is(player);
    }

    private void setNewOwner(PlayerReference newOwner)
    {
        this.commonTab.SetOwner(newOwner);
    }

    private void disbandTeam(EasyButton button)
    {
        this.commonTab.DisbandTeam();
    }

}