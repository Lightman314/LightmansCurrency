package io.github.lightman314.lightmanscurrency.features.atm.tabs;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.helpers.keys.DualKey;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import io.github.lightman314.lightmanscurrency.features.atm.ATMMenu;
import io.github.lightman314.lightmanscurrency.features.atm.ATMTab;
import net.minecraft.resources.Identifier;

public class AccountSettingsTab extends ATMTab {

    public static final Identifier CLIENT_KEY = LCApi.id("account_settings");

    public static final TextEntry TOOLTIP = TextEntry.tooltip(LCApi.MODID,"atm.settings");
    public static final TextEntry GUI_NOTIFICATIONS_DISABLED = TextEntry.gui(LCApi.MODID,"atm.notification.disabled");
    public static final TextEntry GUI_NOTIFICATIONS_DETAILS = TextEntry.gui(LCApi.MODID,"atm.notification.details");
    public static final TextEntry BUTTON_BANK_CARD_RESET = TextEntry.button(LCApi.MODID,"atm.bank_card.reset");
    public static final TextEntry TOOLTIP_BANK_CARD_RESET = TextEntry.tooltip(LCApi.MODID,"atm.bank_card.reset");

    public AccountSettingsTab(ATMMenu menu) { super(menu); }

    @Override
    public boolean usesMoneySlots() { return false; }

    @Override
    public Identifier getClientTabKey() { return CLIENT_KEY; }

    public void setNotificationLevel(DualKey key,MoneyValue amount) {
        if(this.isClient()) {
            this.send(FancyPacketMap.map()
                    .setMap("setNotificationLevel",FancyPacketMap.map()
                            .set("key",LCFancyPacketTypes.DUAL_KEY,key)
                            .set("amount",LCFancyPacketTypes.MONEY,amount)));
            return;
        }
        BankAccount account = this.getAccount();
        if(account != null)
            account.setNotificationLevel(key,amount);
    }

    public void resetBankCardKey() {
        if(this.isClient()) {
            this.send(FancyPacketMap.flag("resetCardKey"));
            return;
        }
        BankAccount account = this.getAccount();
        if(account != null)
            account.resetCards();
    }

    @Override
    public void handleMessage(FancyPacketMap message) {
        if(message.contains("setNotificationLevel")) {
            FancyPacketMap entry = message.getMap("setNotificationLevel");
            DualKey key = entry.get("key",LCFancyPacketTypes.DUAL_KEY);
            MoneyValue amount = entry.get("amount",LCFancyPacketTypes.MONEY);
            this.setNotificationLevel(key,amount);
        }
        if(message.contains("resetCardKey"))
            this.resetBankCardKey();
    }
}
