package io.github.lightman314.lightmanscurrency.client.gui.screen.team;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.menus.teams.TeamManagementClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.teams.tabs.TeamOwnerTab;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;

public class TeamOwnerClientTab extends TeamManagementClientTab<TeamOwnerTab> {

    public TeamOwnerClientTab(@Nonnull Object screen, @Nonnull TeamOwnerTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.of(Items.WRITABLE_BOOK); }

    @Override
    public MutableComponent getTooltip() { return LCText.TOOLTIP_TEAM_OWNER.get(); }

    EditBox newOwnerName;
    EasyButton buttonChangeOwner;

    EasyButton buttonDisbandTeam;

    @Override
    public boolean blockInventoryClosing() { return true; }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.newOwnerName = this.addChild(new EditBox(this.getFont(), screenArea.x + 20, screenArea.y + 20, 160, 20, EasyText.empty()));
        this.newOwnerName.setMaxLength(16);

        this.buttonChangeOwner = this.addChild(new EasyTextButton(screenArea.pos.offset(20, 45), 160, 20, LCText.BUTTON_OWNER_SET_PLAYER.get(), this::setNewOwner)
                .withAddons(EasyAddonHelper.tooltip(LCText.TOOLTIP_WARNING_CANT_BE_UNDONE.getWithStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW))));
        this.buttonChangeOwner.active = false;

        this.buttonDisbandTeam = this.addChild(new EasyTextButton(screenArea.pos.offset(20, 160),160, 20, LCText.BUTTON_TEAM_DISBAND.get(), this::disbandTeam)
                .withAddons(EasyAddonHelper.tooltip(LCText.TOOLTIP_WARNING_CANT_BE_UNDONE.getWithStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW))));

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        ITeam team = this.menu.selectedTeam();
        if(team == null)
            return;

        gui.drawString(LCText.GUI_OWNER_CURRENT.get(team.getOwner().getName(true)), 20, 10, 0x404040);

        TextRenderUtil.drawCenteredText(gui, LCText.GUI_TEAM_ID.get(team.getID()), this.screen.getXSize() / 2, 184, 0x404040);

    }

    @Override
    public void tick() {

        this.buttonChangeOwner.active = !this.newOwnerName.getValue().isBlank();

    }

    private void setNewOwner(EasyButton button)
    {
        if(this.newOwnerName.getValue().isBlank())
            return;

        this.commonTab.SetOwner(this.newOwnerName.getValue());
        this.newOwnerName.setValue("");

    }

    private void disbandTeam(EasyButton button)
    {
        this.commonTab.DisbandTeam();
    }

}
