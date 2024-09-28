package io.github.lightman314.lightmanscurrency.client.gui.screen.team;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.menus.teams.TeamManagementClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.teams.tabs.TeamSalaryPaymentsTab;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TeamSalaryPaymentsClientTab extends TeamManagementClientTab<TeamSalaryPaymentsTab> {

    public TeamSalaryPaymentsClientTab(@Nonnull Object screen, @Nonnull TeamSalaryPaymentsTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.of(ModBlocks.COINPILE_EMERALD); }

    @Nullable
    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TEAM_SALARY_PAYMENTS.get(); }

    EasyButton creativeSalaryToggle;
    MoneyValueWidget memberSalaryInput;
    PlainButton adminSeperationToggle;
    MoneyValueWidget adminSalaryInput;

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        ITeam team = this.menu.selectedTeam();

        this.creativeSalaryToggle = this.addChild(new IconButton(screenArea.pos.offset(screenArea.width - 24,4),this::toggleCreativeSalary,IconUtil.ICON_CREATIVE(this::isSalaryCreative))
                .withAddons(EasyAddonHelper.visibleCheck(() -> this.isSalaryCreative() || LCAdminMode.isAdminPlayer(this.menu.player)),
                        EasyAddonHelper.tooltip(() -> this.isSalaryCreative() ? LCText.TOOLTIP_TEAM_SALARY_PAYMENTS_CREATIVE_DISABLE.get() : LCText.TOOLTIP_TEAM_SALARY_PAYMENTS_CREATIVE_ENABLE.get())));

        this.memberSalaryInput = this.addChild(new MoneyValueWidget(screenArea.pos.offset((screenArea.width / 2) - (MoneyValueWidget.WIDTH / 2), 20), this.memberSalaryInput, team != null ? team.getMemberSalary() : MoneyValue.empty(), this.commonTab::SetMemberSalary));
        this.memberSalaryInput.drawBG = false;
        this.memberSalaryInput.allowFreeInput = false;

        this.adminSeperationToggle = this.addChild(IconAndButtonUtil.checkmarkButton(screenArea.pos.offset(20, 100), this::toggleAdminSeperation,this::isAdminSalarySeperate));

        this.adminSalaryInput = this.addChild(new MoneyValueWidget(screenArea.pos.offset((screenArea.width / 2) - (MoneyValueWidget.WIDTH / 2), 125), this.adminSalaryInput, team != null ? team.getAdminSalary() : MoneyValue.empty(), this.commonTab::SetAdminSalary));
        this.adminSalaryInput.drawBG = false;
        this.adminSalaryInput.allowFreeInput = false;

        this.tick();

    }

    @Override
    public void tick() {
        ITeam team = this.menu.selectedTeam();
        if(team == null)
            return;
        boolean isOwner = this.adminSeperationToggle.visible = team.isOwner(this.menu.player);
        this.adminSalaryInput.visible = isOwner && team.isAdminSalarySeperate();
    }

    private boolean isSalaryCreative() {
        ITeam team = this.menu.selectedTeam();
        return team != null && team.isSalaryCreative();
    }

    private boolean isAdminSalarySeperate() {
        ITeam team = this.menu.selectedTeam();
        return team != null && team.isAdminSalarySeperate();
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        ITeam team = this.menu.selectedTeam();
        if(team == null)
            return;

        gui.drawString(LCText.GUI_TEAM_SALARY_PAYMENTS_MEMBER_SALARY.get(), 20, 10, 0x404040);

        if(team.isOwner(this.menu.player))
        {
            gui.drawString(LCText.GUI_TEAM_SALARY_PAYMENTS_ADMIN_SALARY_SEPERATION.get(), 32, 101, 0x404040);

            if(team.isAdminSalarySeperate())
                gui.drawString(LCText.GUI_TEAM_SALARY_PAYMENTS_ADMIN_SALARY.get(), 20, 115, 0x404040);
        }

    }

    private void toggleCreativeSalary(EasyButton button)
    {
        this.commonTab.MakeSalaryMoneyCreative(!this.isSalaryCreative());
    }

    private void toggleAdminSeperation(EasyButton button)
    {
        this.commonTab.SetAdminSalarySeperate(!this.isAdminSalarySeperate());
    }

}