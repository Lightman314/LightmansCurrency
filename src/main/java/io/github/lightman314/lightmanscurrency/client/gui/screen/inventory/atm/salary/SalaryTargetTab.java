package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm.salary;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.salary.CustomTarget;
import io.github.lightman314.lightmanscurrency.api.money.bank.salary.SalaryData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.BankAccountSelectionWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.BankAccountSelectButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SalaryTargetTab extends SalarySubTab.EditTab {

    public SalaryTargetTab(SalaryTab tab, ATMScreen screen) { super(tab, screen); }

    @Override
    public IconData getIcon() { return IconUtil.ICON_ALEX_HEAD; }

    @Nullable
    @Override
    public Component getTooltip() { return LCText.TOOLTIP_BANK_SALARY_TARGETS.get(); }

    private final Map<String, CustomTarget> extraOptions = new HashMap<>();

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {
        IBankAccount account = this.menu.getBankAccount();
        this.extraOptions.clear();
        if(account != null)
            this.extraOptions.putAll(account.extraSalaryTargets());
        int extraSpace = this.extraOptions.size() * 10;
        if(!this.extraOptions.isEmpty())
            extraSpace += 15;

        int yPos = 20;
        for(var entry : this.extraOptions.entrySet())
        {
            this.addChild(PlainButton.builder()
                    .position(screenArea.pos.offset(20,yPos))
                    .sprite(SpriteUtil.createCheckbox(() -> this.isOptionEnabled(entry.getKey())))
                    .pressAction(() -> this.toggleExtraOption(entry.getKey()))
                    .build());
            yPos += 10;
        }

        int availableSpace = 130 - extraSpace;
        this.addChild(BankAccountSelectionWidget.builder()
                .position(screenArea.pos.offset(20,10 + extraSpace))
                .width(screenArea.width - 40)
                .rows(availableSpace / BankAccountSelectButton.HEIGHT)
                .filter(this::accountAllowed)
                .highlight(this::accountSelected)
                .handler(this::toggleTarget)
                .build());

    }

    @Override
    public void renderBG(EasyGuiGraphics gui) {

        if(!this.extraOptions.isEmpty())
        {
            //Render Custom Options Label
            gui.drawString(LCText.GUI_BANK_SALARY_TARGETS_CUSTOM.get(),20,5,0x404040);
            int yPos = 21;
            for(CustomTarget option : this.extraOptions.values())
            {
                gui.drawString(option.getName(),32,yPos,0x404040);
                yPos += 10;
            }
        }
    }

    private boolean accountAllowed(BankReference reference) { return !reference.equals(this.menu.getBankAccountReference()); }

    private boolean accountSelected(BankReference reference)
    {
        SalaryData salary = this.parent.getSelectedSalary();
        if(salary == null)
            return false;
        return salary.getAllTargets().stream().anyMatch(br -> br.equals(reference));
    }

    private boolean isOptionEnabled(String option)
    {
        SalaryData salary = this.parent.getSelectedSalary();
        if(salary == null)
            return false;
        return salary.getCustomTargetSelections().contains(option);
    }

    private void toggleExtraOption(String option)
    {
        this.SendEditMessage(this.builder().setString("CustomTarget",option)
                .setBoolean("NewState",!this.isOptionEnabled(option)));
    }

    private void toggleTarget(BankReference account)
    {
        this.SendEditMessage(this.builder().setCompound("DirectTarget",account.save())
                .setBoolean("NewState",!this.accountSelected(account)));
    }

    @Override
    public boolean blockInventoryClosing() { return true; }

}