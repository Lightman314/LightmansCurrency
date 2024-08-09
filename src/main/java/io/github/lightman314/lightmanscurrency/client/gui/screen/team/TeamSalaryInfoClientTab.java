package io.github.lightman314.lightmanscurrency.client.gui.screen.team;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.menus.teams.TeamManagementClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.teams.tabs.TeamSalaryInfoTab;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class TeamSalaryInfoClientTab extends TeamManagementClientTab<TeamSalaryInfoTab> {

    public TeamSalaryInfoClientTab(@Nonnull Object screen, @Nonnull TeamSalaryInfoTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.of(IconUtil.ICON_TRADER_ALT); }

    @Nullable
    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TEAM_SALARY_INFO.get(); }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) { }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {
        ITeam team = this.menu.selectedTeam();
        if(team == null)
            return;
        int center = this.screen.getXSize() / 2;

        //Disabled warning
        if(!team.isAutoSalaryEnabled())
            TextRenderUtil.drawCenteredText(gui, LCText.GUI_TEAM_SALARY_INFO_DISABLED.get(), center, 16, ChatFormatting.YELLOW.getColor(), true);

        //Delay
        if(team.getSalaryDelay() > 0)
        {
            TextRenderUtil.drawCenteredText(gui, LCText.GUI_TEAM_SALARY_INFO_DELAY.get(new TimeUtil.TimeData(team.getSalaryDelay()).getShortString()), center, 30, 0x404040);
            if(team.isAutoSalaryEnabled())
            {
                long nextTrigger = team.getLastSalaryTime() + team.getSalaryDelay();
                long timeUntilNextTrigger = Math.max(0,nextTrigger - TimeUtil.getCurrentTime());
                TextRenderUtil.drawCenteredText(gui, LCText.GUI_TEAM_SALARY_INFO_NEXT_TRIGGER.get(new TimeUtil.TimeData(timeUntilNextTrigger).getShortString(2)), center, 42, 0x404040);
            }
        }

        //Member Salary
        if(!team.getMemberSalary().isEmpty())
            TextRenderUtil.drawCenteredText(gui, LCText.GUI_TEAM_SALARY_INFO_SALARY_MEMBERS.get(team.getMemberSalary().getText()), center, 54, 0x404040);

        //Admin Only Info
        if(team.isAdmin(this.menu.player))
        {
            //Admin Salary
            if(team.isAdminSalarySeperate() && !team.getAdminSalary().isEmpty())
                TextRenderUtil.drawCenteredText(gui, LCText.GUI_TEAM_SALARY_INFO_SALARY_ADMINS.get(team.getAdminSalary().getText()), center, 66, 0x404040);

            //Required Funds
            List<MoneyValue> values = team.getTotalSalaryCost();
            if(!values.isEmpty())
            {
                gui.drawString(LCText.GUI_TEAM_SALARY_INFO_REQUIRED_FUNDS.get(), 20, 80, 0x404040);

                int offset = 10;
                for(MoneyValue value : team.getTotalSalaryCost())
                {
                    gui.drawString(value.getText(),25, 80 + offset, 0x404040);
                    offset += 10;
                }
            }

            //Insufficient Funds warning
            if(!team.canAffordNextSalary())
                TextRenderUtil.drawCenteredMultilineText(gui, LCText.GUI_TEAM_SALARY_INFO_INSUFFICIENT_FUNDS.get(), 20, this.screen.getXSize() - 40, 120, ChatFormatting.YELLOW.getColor(), true);

            //Failed Salary Attempt warning
            if(team.failedLastSalaryAttempt())
                TextRenderUtil.drawCenteredMultilineText(gui, LCText.GUI_TEAM_SALARY_INFO_LAST_ATTEMPT_FAILED.get(), 20, this.screen.getXSize() - 40, 165, ChatFormatting.YELLOW.getColor(), true);

        }

    }

}