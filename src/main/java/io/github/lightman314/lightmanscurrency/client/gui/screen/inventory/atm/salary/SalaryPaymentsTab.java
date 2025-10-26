package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm.salary;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.money.bank.salary.SalaryData;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SalaryPaymentsTab extends SalarySubTab.EditTab {


    public SalaryPaymentsTab(SalaryTab tab, ATMScreen screen) { super(tab, screen); }

    @Override
    public IconData getIcon() { return ItemIcon.ofItem(ModBlocks.COINPILE_EMERALD); }
    @Nullable
    @Override
    public Component getTooltip() { return LCText.TOOLTIP_BANK_SALARY_PAYMENTS.get(); }

    EasyButton creativeSalaryToggle;
    TextBoxWrapper<String> nameInput;
    MoneyValueWidget memberSalaryInput;

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        SalaryData salary = this.parent.getSelectedSalary();

        this.creativeSalaryToggle = this.addChild(IconButton.builder()
                .position(screenArea.pos.offset(screenArea.width - 24,4))
                .pressAction(this::toggleCreativeSalary)
                .icon(IconUtil.ICON_CREATIVE_TOGGLE(this::isSalaryCreative))
                .addon(EasyAddonHelper.visibleCheck(() -> this.isSalaryCreative() || LCAdminMode.isAdminPlayer(this.menu.player)))
                .addon(EasyAddonHelper.tooltip(() -> this.isSalaryCreative() ? LCText.TOOLTIP_BANK_SALARY_PAYMENTS_CREATIVE_DISABLE.get() : LCText.TOOLTIP_BANK_SALARY_PAYMENTS_CREATIVE_ENABLE.get()))
                .build());

        this.nameInput = this.addChild(TextInputUtil.stringBuilder()
                .position(screenArea.pos.offset(20,20))
                .width(screenArea.width - 40)
                .startingString(salary != null ? salary.getInternalName() : "")
                .maxLength(16)
                .handler(this::SetName)
                .wrap().build());

        this.memberSalaryInput = this.addChild(MoneyValueWidget.builder()
                .position(screenArea.pos.offset(screenArea.width / 2 - MoneyValueWidget.WIDTH / 2, 60))
                .oldIfNotFirst(firstOpen,this.memberSalaryInput)
                .startingValue(salary == null ? MoneyValue.empty() : salary.getSalary())
                .valueHandler(this::SetSalary)
                .blockFreeInputs()
                .build());

    }

    private boolean isSalaryCreative() {
        SalaryData salary = this.parent.getSelectedSalary();
        return salary != null && salary.isSalaryCreative();
    }

    @Override
    public void renderBG(EasyGuiGraphics gui) {

        SalaryData salary = this.parent.getSelectedSalary();
        if(salary == null)
            return;

        //Name Label
        gui.drawString(LCText.GUI_NAME.get(),20,10,0x404040);

        //Salary Label
        gui.drawString(LCText.GUI_BANK_SALARY_PAYMENTS_MEMBER_SALARY.get(), 20, 50, 0x404040);

    }

    private void toggleCreativeSalary()
    {
        this.SendEditMessage(this.builder()
                .setBoolean("CreativeSalary",!this.isSalaryCreative()));
    }

    private void SetName(String newName)
    {
        this.SendEditMessage(this.builder()
                .setString("ChangeName",newName));
    }

    private void SetSalary(MoneyValue salary)
    {
        this.SendEditMessage(this.builder()
                .setMoneyValue("Salary",salary));
    }

    @Override
    public boolean blockInventoryClosing() { return true; }

}
