package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm.salary;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.salary.SalaryData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SalaryListTab extends SalarySubTab implements IScrollable {

    public SalaryListTab(SalaryTab tab, ATMScreen screen) { super(tab, screen); }

    public static final int ROWS = 5;

    @Override
    public IconData getIcon() { return IconUtil.ICON_DISCOUNT_LIST; }
    @Nullable
    @Override
    public Component getTooltip() { return LCText.TOOLTIP_BANK_SALARY_LIST.get(); }

    private int scroll = 0;
    @Override
    public int currentScroll() { return this.scroll; }
    @Override
    public void setScroll(int newScroll) { this.scroll = Math.max(newScroll,0); }
    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(ROWS,this.getVisibleData().size()); }

    Component tooltip = null;

    private List<SalaryData> getVisibleData()
    {
        List<SalaryData> result = new ArrayList<>();
        BankReference br = this.menu.getBankAccountReference();
        if(br == null)
            return result;
        int accessLevel = br.salaryPermission(this.menu.player);
        if(accessLevel < 1)
            return result;
        IBankAccount account = br.get();
        if(account != null)
            return account.getSalaries();
        return new ArrayList<>();
    }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        for(int i = 0; i < ROWS; ++i)
        {
            final int index = i;
            //Add selection button
            this.addChild(EasyTextButton.builder()
                    .position(screenArea.pos.offset(screenArea.width - 55,40 + (20 * i)))
                    .width(40)
                    .text(this::getSelectionText)
                    .addon(EasyAddonHelper.activeCheck(() -> this.notSelected(index)))
                    .addon(EasyAddonHelper.visibleCheck(() -> this.optionExists(index)))
                    .pressAction(() -> this.selectSalary(index))
                    .build());
        }

        //Add Creation Button
        this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(30,15))
                .width(screenArea.width - 60)
                .text(LCText.BUTTON_BANK_SALARY_LIST_CREATE.get())
                .pressAction(this::createNewSalary)
                .addon(EasyAddonHelper.visibleCheck(() -> this.parent.getSalaryAccess() >= SalaryData.PERM_EDIT))
                .build());

        //Add Scroll Listener and scroll bar
        this.addChild(ScrollListener.builder()
                .listener(this)
                .position(screenArea.pos.offset(15,40))
                .size(screenArea.width - 30,20 * ROWS)
                .build());
        this.addChild(ScrollBarWidget.builder()
                .position(screenArea.pos.offset(screenArea.width - 15,40))
                .height(20 * ROWS)
                .scrollable(this)
                .build());

    }

    protected Component getSelectionText()
    {
        int level = this.parent.getSalaryAccess();
        return level >= SalaryData.PERM_EDIT ? LCText.BUTTON_BANK_SALARY_LIST_EDIT.get() : LCText.BUTTON_BANK_SALARY_LIST_VIEW.get();
    }

    protected boolean optionExists(int localIndex) { return this.getLocalSalary(localIndex) != null; }

    protected boolean notSelected(int localIndex) { return this.parent.getSelectedSalary() != this.getLocalSalary(localIndex); }

    @Nullable
    protected SalaryData getLocalSalary(int localIndex)
    {
        int index = this.scroll + localIndex;
        List<SalaryData> options = this.getVisibleData();
        if(index < 0 || index >= options.size())
            return null;
        return options.get(index);
    }

    @Override
    public void renderBG(EasyGuiGraphics gui) {
        //Render each visible section
        this.validateScroll();
        SalaryData selectedSalary = this.parent.getSelectedSalary();
        int availableWidth = this.screen.getXSize() - 70;
        this.tooltip = null;
        for(int i = 0; i < ROWS; ++i)
        {
            SalaryData data = this.getLocalSalary(i);
            if(data == null)
                break;
            Component name = data.getName();
            if(gui.font.width(name) > availableWidth)
            {
                //Set the name to the tooltip if hovering over it
                if(ScreenArea.of(this.screen.getCorner().offset(20,40 + (20 * i)),availableWidth,20).isMouseInArea(gui.mousePos))
                    this.tooltip = name;
                name = TextRenderUtil.fitString(name,availableWidth);
            }
            int color = data == this.parent.getSelectedSalary() ? 0x40FF40 : 0x404040;
            gui.drawString(name,15,50 + (20 * i),color);
        }
    }

    @Override
    public void renderAfterWidgets(EasyGuiGraphics gui) {
        if(this.tooltip != null)
            gui.renderTooltip(this.tooltip);
    }

    private void selectSalary(int localIndex)
    {
        List<SalaryData> allOptions = this.parent.getAllSalaryOptions();
        SalaryData entry = this.getLocalSalary(localIndex);
        if(entry == null)
            return;
        this.parent.setSelectedSalary(allOptions.indexOf(entry));
    }

    public void createNewSalary() { this.menu.SendMessage(this.builder().setFlag("CreateSalaryOption")); }

}
