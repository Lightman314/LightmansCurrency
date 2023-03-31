package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TeamSelectWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import java.util.List;

public class OwnershipTab extends SettingsSubTab {

    public OwnershipTab(@Nonnull TraderSettingsClientTab parent) { super(parent); }

    EditBox newOwnerInput;
    Button buttonSetOwner;
    TeamSelectWidget teamSelection;
    Button buttonSetTeamOwner;

    long selectedTeam = -1;
    List<Team> teamList = Lists.newArrayList();

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.of(ItemRenderUtil.getAlexHead()); }

    @Override
    public MutableComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.settings.owner"); }

    @Override
    public boolean canOpen() { return this.menu.hasPermission(Permissions.TRANSFER_OWNERSHIP); }

    @Override
    public void onOpen() {

        this.newOwnerInput = this.addWidget(new EditBox(this.font, this.screen.getGuiLeft() + 20, this.screen.getGuiTop() + 20, 160, 20, EasyText.empty()));
        this.newOwnerInput.setMaxLength(16);

        this.buttonSetOwner = this.addWidget(EasyButton.builder(EasyText.translatable("gui.button.lightmanscurrency.set_owner"), this::setOwner).pos(this.screen.getGuiLeft() + 20, this.screen.getGuiTop() + 41).size(160, 20).build());
        this.buttonSetOwner.active = false;

        this.teamSelection = this.addWidget(new TeamSelectWidget(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 65, 3, () -> this.teamList, this::getSelectedTeam, this::selectTeam));
        this.teamSelection.init(this::addWidget, this.font);

        this.buttonSetTeamOwner = this.addWidget(EasyButton.builder(EasyText.translatable("gui.button.lightmanscurrency.set_owner"), this::setTeamOwner).pos(this.screen.getGuiLeft() + 20, this.screen.getGuiTop() + 130).size(160, 20).build());
        this.buttonSetTeamOwner.active = false;

    }

    @Override
    public void onClose() {
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
    public void renderBG(@Nonnull PoseStack pose, int mouseX, int mouseY, float partialTicks) {

        TraderData trader = this.menu.getTrader();
        if(trader != null)
            this.font.draw(pose, EasyText.translatable("gui.button.lightmanscurrency.team.owner", trader.getOwner().getOwnerName(true)), this.screen.getGuiLeft() + 20, this.screen.getGuiTop() + 10, 0x404040);

    }

    @Override
    public void renderTooltips(@Nonnull PoseStack pose, int mouseX, int mouseY) {

        if(this.buttonSetOwner.isMouseOver(mouseX, mouseY) || this.buttonSetTeamOwner.isMouseOver(mouseX, mouseY))
        {
            screen.renderTooltip(pose, EasyText.translatable("tooltip.lightmanscurrency.warning").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW), mouseX, mouseY);
        }

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

    private void setOwner(Button button)
    {
        if(this.newOwnerInput.getValue().isBlank())
            return;
        CompoundTag message = new CompoundTag();
        message.putString("ChangePlayerOwner", this.newOwnerInput.getValue());
        this.sendNetworkMessage(message);
        this.newOwnerInput.setValue("");
    }

    private void setTeamOwner(Button button)
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