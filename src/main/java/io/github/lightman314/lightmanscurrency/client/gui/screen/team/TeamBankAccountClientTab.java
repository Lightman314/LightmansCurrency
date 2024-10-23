package io.github.lightman314.lightmanscurrency.client.gui.screen.team;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.menus.teams.TeamManagementClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.teams.tabs.TeamBankAccountTab;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public class TeamBankAccountClientTab extends TeamManagementClientTab<TeamBankAccountTab> {

    public TeamBankAccountClientTab(@Nonnull Object screen, @Nonnull TeamBankAccountTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.of(ModBlocks.COINPILE_GOLD); }

    @Override
    public MutableComponent getTooltip() { return LCText.TOOLTIP_TEAM_BANK.get(); }

    EasyButton buttonCreateBankAccount;
    EasyButton buttonToggleAccountLimit;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.buttonCreateBankAccount = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(20,20))
                .width(160)
                .text(LCText.BUTTON_TEAM_BANK_CREATE)
                .pressAction(this::createBankAccount)
                .build());

        this.buttonToggleAccountLimit = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(20,60))
                .width(160)
                .text(this::getBankLimitText)
                .pressAction(this::toggleBankLimit)
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
        }

    }

    @Override
    public void tick() {

        ITeam team = this.menu.selectedTeam();
        if(team == null)
            return;

        this.buttonCreateBankAccount.active = !team.hasBankAccount();

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

    private int getBankLimit()
    {
        ITeam team = this.menu.selectedTeam();
        return team == null ? 2 : team.getBankLimit();
    }

    private Component getBankLimitText() { return LCText.BUTTON_TEAM_BANK_LIMIT.get(Owner.getOwnerLevelBlurb(this.getBankLimit())); }

}
