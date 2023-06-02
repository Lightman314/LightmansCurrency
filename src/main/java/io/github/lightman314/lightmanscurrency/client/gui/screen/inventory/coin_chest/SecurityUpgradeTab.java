package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_chest;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.CoinChestScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TeamSelectWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TeamButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.ownership.OwnerData;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestSecurityUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestUpgradeData;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class SecurityUpgradeTab extends CoinChestTab.Upgrade {

    public SecurityUpgradeTab(CoinChestUpgradeData data, Object screen) { super(data, screen); }

    @Override
    public boolean isVisible() {
        CoinChestUpgradeData data = this.getUpgradeData();
        if(data != null && data.upgrade instanceof CoinChestSecurityUpgrade upgrade)
            return upgrade.isAdmin(this.screen.be, data, this.screen.getMenu().player);
        return false;
    }

    @Override
    public boolean coinSlotsVisible() { return false; }

    @Override
    public boolean blockInventoryClosing() { return true; }

    @Override
    public boolean titleVisible() { return false; }

    EditBox newOwnerInput;
    Button buttonSetOwner;
    TeamSelectWidget teamSelection;
    Button buttonSetTeamOwner;

    long selectedTeam = -1;
    List<Team> teamList = Lists.newArrayList();

    IconButton buttonSetSelfOwner;

    @Override
    public void init() {

        this.newOwnerInput = this.screen.addRenderableTabWidget(new EditBox(this.font, this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 18, this.screen.getXSize() - 20, 20, EasyText.empty()));
        this.newOwnerInput.setMaxLength(16);

        this.buttonSetOwner = this.screen.addRenderableTabWidget(Button.builder(EasyText.translatable("gui.button.lightmanscurrency.set_owner"), this::setOwner).pos(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 39).size(this.screen.getXSize() - 20, 20).build());
        this.buttonSetOwner.active = false;

        this.teamSelection = this.screen.addRenderableTabWidget(new TeamSelectWidget(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 63, 3, TeamButton.Size.NORMAL, () -> this.teamList, this::getSelectedTeam, this::selectTeam));
        this.teamSelection.init(this.screen::addRenderableTabWidget, this.font);

        this.buttonSetTeamOwner = this.screen.addRenderableTabWidget(Button.builder(EasyText.translatable("gui.button.lightmanscurrency.set_owner"), this::setTeamOwner).pos(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 124).size(this.screen.getXSize() - 20, 20).build());
        this.buttonSetTeamOwner.active = false;

        this.buttonSetSelfOwner = this.screen.addRenderableTabWidget(new IconButton(this.screen.getGuiLeft() + this.screen.getXSize() - IconButton.SIZE - 3, this.screen.getGuiTop() + 3, this::setSelfOwner, IconData.of(ItemRenderUtil.getAlexHead()), new IconAndButtonUtil.SimpleTooltip(EasyText.translatable("tooltip.lightmanscurrency.settings.owner.self"))));

        this.tick();

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

    private Component getOwnerName()
    {
        CoinChestUpgradeData data = this.getUpgradeData();
        if(data != null && data.upgrade instanceof CoinChestSecurityUpgrade upgrade)
            return EasyText.literal(upgrade.parseOwnerData(this.menu.be, data).getOwnerName(true));
        return EasyText.translatable("gui.button.lightmanscurrency.team.owner.null");
    }

    @Override
    public void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) {

        this.font.draw(pose, TextRenderUtil.fitString(EasyText.translatable("gui.button.lightmanscurrency.team.owner", this.getOwnerName()), this.screen.getXSize() - 20), this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, 0x404040);

    }

    @Override
    public void postRender(PoseStack pose, int mouseX, int mouseY) {

        if(this.buttonSetOwner.isMouseOver(mouseX, mouseY) || this.buttonSetTeamOwner.isMouseOver(mouseX, mouseY))
            this.screen.renderTooltip(pose, EasyText.translatable("tooltip.lightmanscurrency.warning").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW), mouseX, mouseY);

    }

    private void setSelfOwner(Button button)
    {
        this.menu.SendMessageToServer(LazyPacketData.builder().setBoolean("SetSelfOwner", true));
    }

    private void setOwner(Button button)
    {
        if(this.newOwnerInput.getValue().isBlank())
            return;
        this.menu.SendMessageToServer(LazyPacketData.builder().setString("SetPlayerOwner", this.newOwnerInput.getValue()));
        this.newOwnerInput.setValue("");
    }

    private void setTeamOwner(Button button) {
        if(this.selectedTeam < 0)
            return;
        this.menu.SendMessageToServer(LazyPacketData.builder().setLong("SetTeamOwner", this.selectedTeam));
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

        this.newOwnerInput.tick();

        this.buttonSetOwner.active = !this.newOwnerInput.getValue().isBlank();
        this.buttonSetTeamOwner.active = this.getSelectedTeam() != null;

        CoinChestUpgradeData data = this.getUpgradeData();
        if(data != null && data.upgrade instanceof CoinChestSecurityUpgrade upgrade)
        {
            OwnerData owner = upgrade.parseOwnerData(this.menu.be, data);
            this.buttonSetSelfOwner.visible = !owner.hasOwner() || owner.hasTeam() || !owner.getPlayer().is(this.menu.player);
        }
        else
            this.buttonSetSelfOwner.visible = false;
    }

    @Override
    public void onClose() {
        //Reset the selected team & team list to save space
        this.selectedTeam = -1;
        this.teamList = new ArrayList<>();
    }

}
