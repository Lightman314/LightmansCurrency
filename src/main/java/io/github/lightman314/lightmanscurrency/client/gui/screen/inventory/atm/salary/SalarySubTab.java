package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm.salary;

import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.salary.SalaryData;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.common.menus.ATMMenu;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SalarySubTab extends EasyTab {

    protected final SalaryTab parent;
    protected final ATMScreen screen;
    protected final ATMMenu menu;
    public SalarySubTab(SalaryTab tab,ATMScreen screen) { super(screen); this.parent = tab; this.screen = screen; this.menu = this.screen.getMenu(); }

    public boolean visible(Player player, @Nullable BankReference account, @Nullable SalaryData selectedSalary) { return true; }

    public abstract static class EditTab extends SalarySubTab
    {
        public EditTab(SalaryTab tab, ATMScreen screen) { super(tab, screen); }
        public int getRequiredPermissions(Player player,SalaryData selectedSalary) { return 3; }
        @Override
        public boolean visible(Player player, @Nullable BankReference account, @Nullable SalaryData selectedSalary) {
            if(account != null && selectedSalary != null)
                return account.salaryPermission(player) >= this.getRequiredPermissions(player,selectedSalary);
            return false;
        }
    }

    public boolean renderInventoryLabel() { return true; }

    protected final void SendEditMessage(LazyPacketData.Builder builder) { this.parent.sendEditMessage(builder); }

}
