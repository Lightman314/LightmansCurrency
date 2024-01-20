package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_chest;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TeamSelectWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TeamButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestSecurityUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestUpgradeData;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
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
    EasyButton buttonSetOwner;
    TeamSelectWidget teamSelection;
    EasyButton buttonSetTeamOwner;

    long selectedTeam = -1;
    List<Team> teamList = Lists.newArrayList();

    IconButton buttonSetSelfOwner;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.newOwnerInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 10, screenArea.y + 18, this.screen.getXSize() - 20, 20, EasyText.empty()));
        this.newOwnerInput.setMaxLength(16);

        this.buttonSetOwner = this.addChild(new EasyTextButton(screenArea.pos.offset(10, 39), screenArea.width - 20, 20, EasyText.translatable("gui.button.lightmanscurrency.set_owner"), this::setOwner)
                .withAddons(EasyAddonHelper.tooltip(IconAndButtonUtil.TOOLTIP_CANNOT_BE_UNDONE)));
        this.buttonSetOwner.active = false;

        this.teamSelection = this.addChild(new TeamSelectWidget(screenArea.pos.offset(10, 63), 3, TeamButton.Size.NORMAL, () -> this.teamList, this::getSelectedTeam, this::selectTeam));

        this.buttonSetTeamOwner = this.addChild(new EasyTextButton(screenArea.pos.offset(10, 124), screenArea.width - 20, 20, EasyText.translatable("gui.button.lightmanscurrency.set_owner"), this::setTeamOwner)
                .withAddons(EasyAddonHelper.tooltip(IconAndButtonUtil.TOOLTIP_CANNOT_BE_UNDONE)));
        this.buttonSetTeamOwner.active = false;

        this.buttonSetSelfOwner = this.addChild(new IconButton(this.screen.getGuiLeft() + this.screen.getXSize() - IconButton.SIZE - 3, this.screen.getGuiTop() + 3, this::setSelfOwner, IconAndButtonUtil.ICON_ALEX_HEAD)
                .withAddons(EasyAddonHelper.tooltip(EasyText.translatable("tooltip.lightmanscurrency.settings.owner.self"))));

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
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        gui.drawString(TextRenderUtil.fitString(EasyText.translatable("gui.button.lightmanscurrency.team.owner", this.getOwnerName()), this.screen.getXSize() - 20), 8, 6, 0x404040);

    }

    private void setSelfOwner(EasyButton button)
    {
        this.menu.SendMessageToServer(LazyPacketData.builder().setBoolean("SetSelfOwner", true));
    }

    private void setOwner(EasyButton button)
    {
        if(this.newOwnerInput.getValue().isBlank())
            return;
        this.menu.SendMessageToServer(LazyPacketData.builder().setString("SetPlayerOwner", this.newOwnerInput.getValue()));
        this.newOwnerInput.setValue("");
    }

    private void setTeamOwner(EasyButton button) {
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
    public void closeAction() {
        //Reset the selected team & team list to saveItem space
        this.selectedTeam = -1;
        this.teamList = new ArrayList<>();
    }

}
