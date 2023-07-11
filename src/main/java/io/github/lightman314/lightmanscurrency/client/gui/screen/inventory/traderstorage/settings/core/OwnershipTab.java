package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TeamSelectWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import java.util.List;

public class OwnershipTab extends SettingsSubTab {

    public OwnershipTab(@Nonnull TraderSettingsClientTab parent) { super(parent); }

    EditBox newOwnerInput;
    EasyButton buttonSetOwner;
    TeamSelectWidget teamSelection;
    EasyButton buttonSetTeamOwner;

    long selectedTeam = -1;
    List<Team> teamList = Lists.newArrayList();

    @Nonnull
    @Override
    public IconData getIcon() { return IconAndButtonUtil.ICON_ALEX_HEAD; }

    @Override
    public MutableComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.settings.owner"); }

    @Override
    public boolean canOpen() { return this.menu.hasPermission(Permissions.TRANSFER_OWNERSHIP); }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.newOwnerInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 20, screenArea.y + 20, 160, 20, EasyText.empty()));
        this.newOwnerInput.setMaxLength(16);

        this.buttonSetOwner = this.addChild(new EasyTextButton(screenArea.pos.offset(20, 41), 160, 20, EasyText.translatable("gui.button.lightmanscurrency.set_owner"), this::setOwner)
                .withAddons(EasyAddonHelper.tooltip(IconAndButtonUtil.TOOLTIP_CANNOT_BE_UNDONE)));
        this.buttonSetOwner.active = false;

        this.teamSelection = this.addChild(new TeamSelectWidget(screenArea.pos.offset(10, 65), 3, () -> this.teamList, this::getSelectedTeam, this::selectTeam));

        this.buttonSetTeamOwner = this.addChild(new EasyTextButton(screenArea.pos.offset(20, 130), 160, 20, EasyText.translatable("gui.button.lightmanscurrency.set_owner"), this::setTeamOwner)
                .withAddons(EasyAddonHelper.tooltip(IconAndButtonUtil.TOOLTIP_CANNOT_BE_UNDONE)));
        this.buttonSetTeamOwner.active = false;

    }

    @Override
    protected void onSubtabClose() {
        //Reset the selected team & team list to save space
        this.selectedTeam = -1;
        this.teamList = Lists.newArrayList();
    }

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
        this.teamList = Lists.newArrayList();
        List<Team> allTeams = TeamSaveData.GetAllTeams(true);
        allTeams.forEach(team ->{
            if(team.isMember(this.menu.player))
                this.teamList.add(team);
        });
        this.teamList.sort(Team.sorterFor(this.menu.player));
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        TraderData trader = this.menu.getTrader();
        if(trader != null)
            gui.drawString(EasyText.translatable("gui.button.lightmanscurrency.team.owner", trader.getOwner().getOwnerName(true)), 20, 10, 0x404040);

    }

    @Override
    public void tick() {

        this.refreshTeamList();

        this.newOwnerInput.tick();

        this.buttonSetOwner.active = !this.newOwnerInput.getValue().isBlank();
        this.buttonSetTeamOwner.active = this.getSelectedTeam() != null;

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

    private void setOwner(EasyButton button)
    {
        if(this.newOwnerInput.getValue().isBlank())
            return;
        CompoundTag message = new CompoundTag();
        message.putString("ChangePlayerOwner", this.newOwnerInput.getValue());
        this.sendNetworkMessage(message);
        this.newOwnerInput.setValue("");
    }

    private void setTeamOwner(EasyButton button)
    {
        if(this.getSelectedTeam() == null)
            return;
        CompoundTag message = new CompoundTag();
        message.putLong("ChangeTeamOwner", this.selectedTeam);
        this.sendNetworkMessage(message);
        this.selectedTeam = -1;
    }

    @Override
    public boolean shouldRenderInventoryText() { return false; }

}
