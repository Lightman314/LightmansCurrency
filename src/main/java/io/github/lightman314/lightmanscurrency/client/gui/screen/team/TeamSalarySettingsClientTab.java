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

    EasyButton toggleLoginRequirementButton;
    EasyButton toggleAutoSalaryButton;
    PlainButton toggleSalaryNotificationButton;
    TimeInputWidget salaryDelayInput;
    EasyButton manualTriggerButton;

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        ITeam team = this.menu.selectedTeam();

        this.toggleAutoSalaryButton = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(20,10))
                .width(screenArea.width - 40)
                .text(this::getToggleButtonText)
                .pressAction(this::ToggleAutoSalary)
                .build());

        this.toggleLoginRequirementButton = this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(20, 38))
                .pressAction(this::ToggleLoginRequirement)
                .sprite(IconAndButtonUtil.SPRITE_CHECK(this::isLoginRequired))
                .build());

        this.toggleSalaryNotificationButton = this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(20,50))
                .pressAction(this::ToggleSalaryNotification)
                .sprite(IconAndButtonUtil.SPRITE_CHECK(this::isSalaryNotificationEnabled))
                .build());

        this.salaryDelayInput = this.addChild(TimeInputWidget.builder()
                .position(screenArea.pos.offset(20,80))
                .spacing(20)
                .unitRange(TimeUtil.TimeUnit.HOUR, TimeUtil.TimeUnit.DAY)
                .handler(this::SetSalaryDelay)
                .startTime(team == null ? 0 : team.getSalaryDelay())
                .build());

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

    private boolean isLoginRequired()
    {
        ITeam team = this.menu.selectedTeam();
        return team != null && team.getLoginRequiredForSalary();
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

        gui.drawString(LCText.GUI_TEAM_SALARY_SETTINGS_REQUIRE_LOGIN.get(), 32, 39, 0x404040);

        gui.drawString(LCText.GUI_TEAM_SALARY_SETTINGS_NOTIFICATION.get(), 32, 51, 0x404040);

        gui.drawString(LCText.GUI_TEAM_SALARY_SETTINGS_DELAY.get(), 20, 65, 0x404040);

        TextRenderUtil.drawCenteredMultilineText(gui, LCText.GUI_TEAM_SALARY_INFO_DELAY.get(new TimeUtil.TimeData(team.getSalaryDelay()).getString()), 10, this.screen.getXSize() - 20, 120, 0x404040);

    }

    private void ToggleAutoSalary() { this.commonTab.SetAutoSalary(!this.isAutoSalaryEnabled()); }

    private void ToggleLoginRequirement() { this.commonTab.SetSalaryLoginRequirement(!this.isLoginRequired()); }

    private void ToggleSalaryNotification(@Nonnull EasyButton button) { this.commonTab.SetSalaryNotification(!this.isSalaryNotificationEnabled()); }

    private void SetSalaryDelay(@Nonnull TimeUtil.TimeData data) { this.commonTab.SetSalaryDelay(data.miliseconds); }

}