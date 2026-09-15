package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.bank_account.notifications.*;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.taxes.notifications.TaxesPaidNotification;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.OutOfStockNotification;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.settings.ChangeSettingNotification;
import io.github.lightman314.lightmanscurrency.features.trader.item.notifications.ItemTradeNotification;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCNotificationTypes {
    private LCNotificationTypes() {}

    public static final DeferredRegister<NotificationType<?>> REGISTER = DeferredRegister.create(LCRegistries.Notifications.NOTIFICATION_TYPE,LCApi.MODID);

    static {
        register("out_of_stock",OutOfStockNotification.TYPE);
        register("bank_low_balance",LowBalanceNotification.TYPE);
        register("bank_interaction_player",BankInteractionNotification.ForPlayer.TYPE);
        register("bank_interaction_machine",BankInteractionNotification.ForMachine.TYPE);
        register("bank_interaction_server",BankInteractionNotification.ForServer.TYPE);
        register("bank_interest",BankInterestNotification.TYPE);
        register("taxes_paid",TaxesPaidNotification.TYPE);
        register("item_trade",ItemTradeNotification.TYPE);
        register("setting_change_dumb",ChangeSettingNotification.Dumb.TYPE);
        register("setting_change_simple",ChangeSettingNotification.Simple.TYPE);
        register("setting_change_advanced",ChangeSettingNotification.Advanced.TYPE);
    }

    private static void register(String name,NotificationType<?> type) {
        REGISTER.register(name,() -> type);
    }

}
