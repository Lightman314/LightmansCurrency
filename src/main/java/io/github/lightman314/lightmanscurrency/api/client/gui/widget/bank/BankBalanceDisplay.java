package io.github.lightman314.lightmanscurrency.api.client.gui.widget.bank;

import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.TextDisplayWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.money.MoneyDisplayHelper;
import io.github.lightman314.lightmanscurrency.features.atm.tabs.AccountInteractionTab;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;

public final class BankBalanceDisplay {

    private BankBalanceDisplay() {}

    public static TextDisplayWidget.Builder getAccountBalanceDisplay(Supplier<BankAccount> accountSource) {
        return TextDisplayWidget.builder().withText(getBalanceText(accountSource))
                .tooltip(TooltipSource.deferredList(getBalanceTooltip(accountSource)))
                .withCenteredText();
    }

    private static Supplier<Component> getBalanceText(Supplier<BankAccount> source) {
        return () -> {
            BankAccount account = source.get();
            //Render the current balance
            return account == null ? AccountInteractionTab.GUI_NO_SELECTED_ACCOUNT.get() : MoneyDisplayHelper.getCyclingValueText(account);
        };
    }

    private static Supplier<List<Component>> getBalanceTooltip(Supplier<BankAccount> source) {
        return () -> {
            BankAccount account = source.get();
            if(account == null)
                return null;
            return MoneyDisplayHelper.contentsAsMultiLineText(account);
        };
    }

}
