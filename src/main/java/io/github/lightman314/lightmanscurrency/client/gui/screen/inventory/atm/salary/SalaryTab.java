package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm.salary;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.salary.SalaryData;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm.ATMTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.WidgetRotation;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SalaryTab extends ATMTab {

    public SalaryTab(ATMScreen screen) {
        super(screen);
        //Initialize sub-tab list
        this.subTabList.add(new SalaryListTab(this,screen));
        this.subTabList.add(new SalaryInfoTab(this,screen));
        this.subTabList.add(new SalarySettingsTab(this,screen));
        this.subTabList.add(new SalaryPaymentsTab(this,screen));
        this.subTabList.add(new SalaryTargetTab(this,screen));
    }

    @Override
    public IconData getIcon() { return IconUtil.ICON_TRADER_ALT; }

    @Nullable
    @Override
    public Component getTooltip() { return LCText.TOOLTIP_BANK_SALARY.get(); }

    private final List<SalarySubTab> subTabList = new ArrayList<>();
    private SalarySubTab currentTab;

    private int selectedSalary = -1;
    public int getSelectedSalaryIndex() { return this.selectedSalary; }
    public void setSelectedSalary(int index) { this.selectedSalary = index; }
    public void clearSelectedSalary() { this.selectedSalary = -1; }
    public List<SalaryData> getAllSalaryOptions()
    {
        IBankAccount account = this.menu.getBankAccount();
        if(account != null)
            return account.getSalaries();
        return new ArrayList<>();
    }
    @Nullable
    public SalaryData getSelectedSalary() {
        if(this.selectedSalary < 0)
            return null;
        IBankAccount account = this.menu.getBankAccount();
        if(account != null)
        {
            if(this.selectedSalary < account.getSalaries().size())
                return account.getSalaries().get(this.selectedSalary);
        }
        return null;
    }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {
        if(firstOpen)
        {
            this.currentTab = this.subTabList.get(0);
            this.clearSelectedSalary();
            this.screen.setCoinSlotsActive(false);
        }

        LazyWidgetPositioner positioner = this.addChild(LazyWidgetPositioner.create(this.screen,LazyWidgetPositioner.createTopdown(WidgetRotation.RIGHT),screenArea.width,0, TabButton.SIZE));
        //Create sub-tab buttons
        for(SalarySubTab subTab : this.subTabList)
        {
            TabButton button = this.addChild(TabButton.builder()
                    .tab(subTab)
                    .addon(EasyAddonHelper.visibleCheck(this.visibleCheck(subTab)))
                    .pressAction(() -> this.changeTab(subTab))
                    .addon(EasyAddonHelper.activeCheck(() -> this.notSelected(subTab)))
                    .build());
            positioner.addWidget(button);
        }
        this.currentTab.onOpen();
    }

    public int getSalaryAccess()
    {
        BankReference br = this.menu.getBankAccountReference();
        if(br == null)
            return 0;
        return br.salaryPermission(this.menu.player);
    }

    protected Supplier<Boolean> visibleCheck(SalarySubTab tab)
    {
        return () -> tab.visible(this.menu.player,this.menu.getBankAccountReference(),this.getSelectedSalary());
    }

    private boolean notSelected(SalarySubTab tab) { return tab != this.currentTab; }

    private void changeTab(SalarySubTab tab)
    {
        if(tab == this.currentTab || tab == null)
            return;
        this.currentTab.onClose();
        this.currentTab = tab;
        this.currentTab.onOpen();
    }

    @Override
    protected void closeAction() {
        this.currentTab.onClose();
        this.screen.setCoinSlotsActive(true);
    }
    @Override
    public void tick() { this.currentTab.tick(); }
    @Override
    public void renderBG(EasyGuiGraphics gui) { this.currentTab.renderBG(gui); }
    @Override
    public void renderAfterWidgets(EasyGuiGraphics gui) { this.currentTab.renderAfterWidgets(gui); }
    @Override
    public boolean blockInventoryClosing() { return this.currentTab.blockInventoryClosing(); }
    @Override
    public boolean renderInventoryLabel() { return this.currentTab.renderInventoryLabel(); }

    @Override
    public void HandleMessage(LazyPacketData message) {
        if(message.contains("SelectSalary"))
            this.setSelectedSalary(message.getInt("SelectSalary"));
    }

    public void sendEditMessage(LazyPacketData.Builder builder)
    {
        if(this.selectedSalary < 0)
            return;
        this.menu.SendMessage(builder.setInt("EditSalary",this.selectedSalary));
    }

}