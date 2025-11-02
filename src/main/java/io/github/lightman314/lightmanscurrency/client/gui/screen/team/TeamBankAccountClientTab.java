package io.github.lightman314.lightmanscurrency.client.gui.screen.team;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.menus.teams.TeamManagementClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.teams.tabs.TeamBankAccountTab;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public class TeamBankAccountClientTab extends TeamManagementClientTab<TeamBankAccountTab> {

    public TeamBankAccountClientTab(@Nonnull Object screen, @Nonnull TeamBankAccountTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return ItemIcon.ofItem(ModBlocks.COINPILE_GOLD); }

    @Override
    public MutableComponent getTooltip() { return LCText.TOOLTIP_TEAM_BANK.get(); }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(20,20))
                .width(160)
                .text(LCText.BUTTON_TEAM_BANK_CREATE)
                .pressAction(this::createBankAccount)
                .addon(EasyAddonHelper.activeCheck(() -> !this.hasBankAccount()))
                .build());

        this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(20,80))
                .width(160)
                .text(this::getBankLimitText)
                .pressAction(this::toggleBankLimit)
                .addon(EasyAddonHelper.visibleCheck(this::hasBankAccount))
                .build());

        this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(20,120))
                .width(160)
                .text(this::getSalaryLimitText)
                .pressAction(this::toggleSalaryLimit)
                .addon(EasyAddonHelper.visibleCheck(this::hasBankAccount))
                .build());

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        ITeam team = this.menu.selectedTeam();
        if(team == null)
            return;

        if(team.hasBankAccount())
        {
            IBankAccount account = team.getBankAccount();
            if(account != null)
                gui.drawString(account.getBalanceText(), 20, 46, 0x404040);

            //Bank Access Label
            gui.drawString(LCText.GUI_TEAM_BANK_ACCESS.get(), 20, 70, 0x404040);

            //Salary Edit Label
            gui.drawString(LCText.GUI_TEAM_BANK_SALARY_EDIT.get(), 20, 110, 0x404040);

        }

    }

    private boolean hasBankAccount() {
        ITeam team = this.menu.selectedTeam();
        return team != null && team.hasBankAccount();
    }

    private void createBankAccount(EasyButton button)
    {
        this.commonTab.CreateBankAccount();
    }

    private void toggleBankLimit(EasyButton button)
    {
        int newLimit = Team.NextBankLimit(this.getBankLimit());
        this.commonTab.ChangeBankAccess(newLimit);
    }

    private void toggleSalaryLimit()
    {
        int newLimit = Team.NextBankLimit(this.getSalaryLimit());
        this.commonTab.ChangeSalaryAccess(newLimit);
    }

    private int getBankLimit()
    {
        ITeam team = this.menu.selectedTeam();
        return team == null ? 2 : team.getBankLimit();
    }

    private int getSalaryLimit()
    {
        ITeam team = this.menu.selectedTeam();
        return team == null ? 2 : team.getBankSalaryEdit();
    }

    private Component getBankLimitText() { return Owner.getOwnerLevelBlurb(this.getBankLimit()); }

    private Component getSalaryLimitText() { return Owner.getOwnerLevelBlurb(this.getSalaryLimit()); }

}