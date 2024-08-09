package io.github.lightman314.lightmanscurrency.client.gui.screen.team;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.teams.TeamManagementClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.teams.tabs.TeamNameTab;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public class TeamNameClientTab extends TeamManagementClientTab<TeamNameTab> {

    public TeamNameClientTab(@Nonnull Object screen, @Nonnull TeamNameTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return LCText.BUTTON_CHANGE_NAME_ICON.icon(); }

    @Override
    public MutableComponent getTooltip() { return LCText.TOOLTIP_TEAM_NAME.get(); }

    EditBox nameInput;
    EasyButton buttonChangeName;

    @Override
    public boolean blockInventoryClosing() { return true; }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.nameInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 20, screenArea.y + 20, 160, 20, EasyText.empty()));
        this.nameInput.setMaxLength(Team.MAX_NAME_LENGTH);
        this.nameInput.setValue(this.safeGetName());

        this.buttonChangeName = this.addChild(new EasyTextButton(screenArea.pos.offset(20, 45), 160, 20, LCText.BUTTON_TEAM_RENAME.get(), this::changeName));
        this.buttonChangeName.active = false;
    }

    private String safeGetName() { return this.menu.selectedTeam() == null ? "NULL" : this.menu.selectedTeam().getName(); }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        gui.drawString(LCText.GUI_TEAM_NAME_CURRENT.get(this.safeGetName()), 20, 10, 0x404040);

    }

    @Override
    public void tick() {

        this.buttonChangeName.active = !this.nameInput.getValue().isBlank() && !this.nameInput.getValue().contentEquals(this.safeGetName());

    }

    private void changeName(EasyButton button)
    {
        if(this.nameInput.getValue().isBlank())
            return;

        this.commonTab.ChangeName(this.nameInput.getValue());

    }

}