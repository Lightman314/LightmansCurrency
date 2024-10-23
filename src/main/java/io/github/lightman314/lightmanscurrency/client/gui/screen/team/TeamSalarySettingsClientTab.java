package io.github.lightman314.lightmanscurrency.client.gui.screen.team;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TimeInputWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.menus.teams.TeamManagementClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.teams.tabs.TeamSalarySettingsTab;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TeamSalarySettingsClientTab extends TeamManagementClientTab<TeamSalarySettingsTab> {

    public TeamSalarySettingsClientTab(@Nonnull Object screen, @Nonnull TeamSalarySettingsTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.of(IconUtil.ICON_SETTINGS,IconData.of(ModBlocks.ATM)); }

    @Nullable
    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TEAM_SALARY_SETTINGS.get(); }

    EasyButton toggleAutoSalaryButton;
    PlainButton toggleSalaryNotificationButton;
    TimeInputWidget salaryDelayInput;
    EasyButton manualTriggerButton;

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        ITeam team = this.menu.selectedTeam();

        this.toggleAutoSalaryButton = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(20,20))
                .width(screenArea.width - 40)
                .text(this::getToggleButtonText)
                .pressAction(this::ToggleAutoSalary)
                .build());

        this.toggleSalaryNotificationButton = this.addChild(IconAndButtonUtil.checkmarkButton(screenArea.pos.offset(20, 45),this::ToggleSalaryNotification,this::isSalaryNotificationEnabled));

        this.salaryDelayInput = this.addChild(new TimeInputWidget(screenArea.pos.offset(20, 80), 20, TimeUtil.TimeUnit.DAY, TimeUtil.TimeUnit.HOUR, this::SetSalaryDelay));
        this.salaryDelayInput.setTime(team == null ? 0 : team.getSalaryDelay());

        this.manualTriggerButton = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(20,150))
                .width(screenArea.width - 40)
                .text(LCText.BUTTON_TEAM_SALARY_SETTINGS_TRIGGER_SALARY)
                .pressAction(this.commonTab::ManuallyTriggerSalary)
                .build());

    }

    private boolean isAutoSalaryEnabled()
    {
        ITeam team = this.menu.selectedTeam();
        return team != null && team.isAutoSalaryEnabled();
    }

    private boolean isSalaryNotificationEnabled()
    {
        ITeam team = this.menu.selectedTeam();
        return team != null && team.getSalaryNotification();
    }

    private Component getToggleButtonText() { return this.isAutoSalaryEnabled() ? LCText.BUTTON_TEAM_SALARY_SETTINGS_DISABLE.get() : LCText.BUTTON_TEAM_SALARY_SETTINGS_ENABLE.get(); }

    @Override
    public void tick() {
        ITeam team = this.menu.selectedTeam();
        if(team == null)
            return;
        boolean salaryPossible = (!team.getMemberSalary().isEmpty() || (team.isAdminSalarySeperate() && !team.getAdminSalary().isEmpty()));
        this.toggleAutoSalaryButton.active = (team.getSalaryDelay() > 0 && salaryPossible) || this.isAutoSalaryEnabled();
        this.manualTriggerButton.active = salaryPossible;
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        ITeam team = this.menu.selectedTeam();
        if(team == null)
            return;

        gui.drawString(LCText.GUI_TEAM_SALARY_SETTINGS_NOTIFICATION.get(), 32, 46, 0x404040);

        gui.drawString(LCText.GUI_TEAM_SALARY_SETTINGS_DELAY.get(), 20, 65, 0x404040);

        TextRenderUtil.drawCenteredMultilineText(gui, LCText.GUI_TEAM_SALARY_INFO_DELAY.get(new TimeUtil.TimeData(team.getSalaryDelay()).getString()), 20, this.screen.getXSize() - 40, 120, 0x404040);

    }

    private void ToggleAutoSalary()
    {
        this.commonTab.SetAutoSalary(!this.isAutoSalaryEnabled());
    }

    private void ToggleSalaryNotification(@Nonnull EasyButton button)
    {
        this.commonTab.SetSalaryNotification(!this.isSalaryNotificationEnabled());
    }

    private void SetSalaryDelay(@Nonnull TimeUtil.TimeData data)
    {
        this.commonTab.SetSalaryDelay(data.miliseconds);
    }

}
