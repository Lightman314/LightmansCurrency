package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm.salary;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.api.money.bank.salary.SalaryData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TimeInputWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SalarySettingsTab extends SalarySubTab.EditTab {

    public SalarySettingsTab(SalaryTab tab, ATMScreen screen) { super(tab, screen); }

    @Override
    public IconData getIcon() { return IconUtil.ICON_SETTINGS; }

    @Nullable
    @Override
    public Component getTooltip() { return LCText.TOOLTIP_BANK_SALARY_SETTINGS.get(); }

    EasyButton toggleLoginRequirementButton;
    EasyButton toggleAutoSalaryButton;
    PlainButton toggleSalaryNotificationButton;
    TimeInputWidget salaryDelayInput;
    EasyButton manualTriggerButton;

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        SalaryData salary = this.parent.getSelectedSalary();

        this.toggleAutoSalaryButton = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(20,10))
                .width(screenArea.width - 40)
                .text(this::getToggleButtonText)
                .pressAction(this::ToggleAutoSalary)
                .build());

        this.toggleLoginRequirementButton = this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(20, 38))
                .pressAction(this::ToggleLoginRequirement)
                .sprite(SpriteUtil.createCheckbox(this::isLoginRequired))
                .build());

        this.toggleSalaryNotificationButton = this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(20,50))
                .pressAction(this::ToggleSalaryNotification)
                .sprite(SpriteUtil.createCheckbox(this::isSalaryNotificationEnabled))
                .build());

        this.salaryDelayInput = this.addChild(TimeInputWidget.builder()
                .position(screenArea.pos.offset(20,80))
                .spacing(10)
                .unitRange(TimeUtil.TimeUnit.HOUR, TimeUtil.TimeUnit.DAY)
                .handler(this::SetSalaryDelay)
                .startTime(salary == null ? 0 : salary.getSalaryDelay())
                .build());

        this.manualTriggerButton = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(20,126))
                .width(screenArea.width - 40)
                .text(LCText.BUTTON_BANK_SALARY_SETTINGS_TRIGGER_SALARY)
                .pressAction(this::ManuallyTriggerSalary)
                .build());

    }

    private boolean isAutoSalaryEnabled()
    {
        SalaryData salary = this.parent.getSelectedSalary();
        return salary != null && salary.isAutoSalaryEnabled();
    }

    private boolean isLoginRequired()
    {
        SalaryData salary = this.parent.getSelectedSalary();
        return salary != null && salary.getLoginRequiredForSalary();
    }

    private boolean isSalaryNotificationEnabled()
    {
        SalaryData salary = this.parent.getSelectedSalary();
        return salary != null && salary.getSalaryNotification();
    }

    private Component getToggleButtonText() { return this.isAutoSalaryEnabled() ? LCText.BUTTON_BANK_SALARY_SETTINGS_DISABLE.get() : LCText.BUTTON_BANK_SALARY_SETTINGS_ENABLE.get(); }

    @Override
    public void tick() {
        SalaryData salary = this.parent.getSelectedSalary();
        if(salary == null)
            return;
        this.toggleAutoSalaryButton.active = (salary.getSalaryDelay() > 0 && !salary.getSalary().isEmpty()) || this.isAutoSalaryEnabled();
        this.manualTriggerButton.active = !salary.getSalary().isEmpty();
    }

    @Override
    public void renderBG(EasyGuiGraphics gui) {

        SalaryData salary = this.parent.getSelectedSalary();
        if(salary == null)
            return;

        gui.drawString(LCText.GUI_BANK_SALARY_SETTINGS_REQUIRE_LOGIN.get(), 32, 39, 0x404040);

        gui.drawString(LCText.GUI_BANK_SALARY_SETTINGS_NOTIFICATION.get(), 32, 51, 0x404040);

        gui.drawString(LCText.GUI_BANK_SALARY_SETTINGS_DELAY.get(), 20, 65, 0x404040);

        if(salary.getSalaryDelay() > 0)
            TextRenderUtil.drawVerticallyCenteredMultilineText(gui,LCText.GUI_BANK_SALARY_INFO_DELAY.get(new TimeUtil.TimeData(salary.getSalaryDelay()).getString()),80,this.screen.getXSize() - 90, 80, 33, 0x404040);

    }

    private void ToggleAutoSalary() {
        this.SendEditMessage(this.builder()
                .setBoolean("EnableAutoSalary",!this.isAutoSalaryEnabled()));
    }

    private void ToggleLoginRequirement() {
        this.SendEditMessage(this.builder()
                .setBoolean("LoginRequirement",!this.isLoginRequired()));
    }

    private void ToggleSalaryNotification(EasyButton button) {
        this.SendEditMessage(this.builder()
                .setBoolean("SalaryNotification", !this.isSalaryNotificationEnabled()));
    }

    private void SetSalaryDelay(TimeUtil.TimeData data) {
        this.SendEditMessage(this.builder()
                .setLong("SalaryDelay",data.miliseconds));
    }

    private void ManuallyTriggerSalary() {
        this.SendEditMessage(this.builder().setFlag("TriggerSalary"));
    }

}
