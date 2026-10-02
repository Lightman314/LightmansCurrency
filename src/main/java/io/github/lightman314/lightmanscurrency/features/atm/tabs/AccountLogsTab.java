package io.github.lightman314.lightmanscurrency.features.atm.tabs;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.features.atm.ATMMenu;
import io.github.lightman314.lightmanscurrency.features.atm.ATMTab;
import net.minecraft.resources.Identifier;

public class AccountLogsTab extends ATMTab {

    public static final Identifier CLIENT_KEY = LCApi.id("account_logs");

    public static final TextEntry TOOLTIP = TextEntry.tooltip(LCApi.MODID,"atm.logs");

    public AccountLogsTab(ATMMenu menu) { super(menu); }

    @Override
    public boolean usesMoneySlots() { return false; }
    @Override
    public Identifier getClientTabKey() { return CLIENT_KEY; }

}
