package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm.salary;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.api.money.bank.salary.SalaryData;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SalaryInfoTab extends SalarySubTab.EditTab {

    public SalaryInfoTab(SalaryTab tab, ATMScreen screen) { super(tab, screen); }

    @Override
    public IconData getIcon() { return IconUtil.ICON_TRADER_ALT; }

    @Nullable
    @Override
    public Component getTooltip() {return LCText.TOOLTIP_BANK_SALARY_INFO.get(); }

    @Override
    public int getRequiredPermissions(Player player, SalaryData selectedSalary) {
        if(selectedSalary.isTarget(player))
            return 1;
        return 2;
    }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) { }

    @Override
    public void renderBG(EasyGuiGraphics gui) {
        SalaryData salary = this.parent.getSelectedSalary();
        if(salary == null)
            return;

        int leftEdge = 6;
        int width = this.screen.getXSize() - 12;
        int center = this.screen.getXSize() / 2;
        int yPos = 16;

        //Disabled warning
        if(!salary.isAutoSalaryEnabled())
            TextRenderUtil.drawCenteredMultilineText(gui,LCText.GUI_BANK_SALARY_INFO_DISABLED.get(),6,width,6,ChatFormatting.YELLOW.getColor(),true);

        //Delay
        if(salary.getSalaryDelay() > 0)
        {
            TextRenderUtil.drawCenteredText(gui, LCText.GUI_BANK_SALARY_INFO_DELAY.get(new TimeUtil.TimeData(salary.getSalaryDelay()).getShortString()), center, 30, 0x404040);
            if(salary.isAutoSalaryEnabled())
            {
                long nextTrigger = salary.getLastSalaryTime() + salary.getSalaryDelay();
                long timeUntilNextTrigger = Math.max(0,nextTrigger - TimeUtil.getCurrentTime());
                TextRenderUtil.drawCenteredText(gui, LCText.GUI_BANK_SALARY_INFO_NEXT_TRIGGER.get(new TimeUtil.TimeData(timeUntilNextTrigger).getShortString(2)), center, 42, 0x404040);
            }
        }

        //Salary
        if(!salary.getSalary().isEmpty())
            TextRenderUtil.drawCenteredText(gui, LCText.GUI_BANK_SALARY_INFO_SALARY.get(salary.getSalary().getText()), center, 54, 0x404040);

        int accessLevel = this.parent.getSalaryAccess();
        //Admin Only Info
        if(accessLevel >= SalaryData.PERM_EDIT)
        {
            //Required Funds
            MoneyValue totalCost = salary.getTotalSalaryCost(false);
            if(!totalCost.isEmpty())
                TextRenderUtil.drawCenteredMultilineText(gui,LCText.GUI_BANK_SALARY_INFO_REQUIRED_FUNDS.get(totalCost.getText()),leftEdge,width,66,0x404040);
            //Required Auto-Salary Funds
            if(salary.getLoginRequiredForSalary())
            {
                totalCost = salary.getTotalSalaryCost(true);
                if(!totalCost.isEmpty())
                    TextRenderUtil.drawCenteredMultilineText(gui,LCText.GUI_BANK_SALARY_INFO_CURRENT_REQUIRED_FUNDS.get(totalCost.getText()), leftEdge,width, 86, 0x404040);
            }

            Component fundsWarning = null;
            if(!salary.canAffordNextSalary(true))
                fundsWarning = LCText.GUI_BANK_SALARY_INFO_INSUFFICIENT_FUNDS.get();
            else if(!salary.canAffordNextSalary(false))
                fundsWarning = LCText.GUI_BANK_SALARY_INFO_POSSIBLE_INSUFFICIENT_FUNDS.get();
            //Insufficient Funds warning
            if(fundsWarning != null)
                TextRenderUtil.drawCenteredMultilineText(gui, fundsWarning, leftEdge, width, 106, ChatFormatting.YELLOW.getColor(), true);

            //Failed Salary Attempt warning
            if(salary.failedLastSalaryAttempt())
                TextRenderUtil.drawCenteredMultilineText(gui, LCText.GUI_BANK_SALARY_INFO_LAST_ATTEMPT_FAILED.get(), leftEdge, width, 136, ChatFormatting.YELLOW.getColor(), true);

        }
    }

    @Override
    public boolean renderInventoryLabel() {
        SalaryData salary = this.parent.getSelectedSalary();
        if(salary == null)
            return super.renderInventoryLabel();
        return !salary.failedLastSalaryAttempt();
    }
}