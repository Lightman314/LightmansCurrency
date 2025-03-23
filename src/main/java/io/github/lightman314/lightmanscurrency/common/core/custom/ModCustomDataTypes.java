package io.github.lightman314.lightmanscurrency.common.core.custom;

import io.github.lightman314.lightmanscurrency.common.core.ModRegistries;
import io.github.lightman314.lightmanscurrency.common.data.types.*;

public class ModCustomDataTypes {

    public static void init() {}

    static {
        ModRegistries.CUSTOM_DATA.register("trader",() -> TraderDataCache.TYPE);
        ModRegistries.CUSTOM_DATA.register("bank_accounts",() -> BankDataCache.TYPE);
        ModRegistries.CUSTOM_DATA.register("teams",() -> TeamDataCache.TYPE);
        ModRegistries.CUSTOM_DATA.register("notifications",() -> NotificationDataCache.TYPE);
        ModRegistries.CUSTOM_DATA.register("ejection_data",() -> EjectionDataCache.TYPE);
        ModRegistries.CUSTOM_DATA.register("tax_entries",() -> TaxDataCache.TYPE);
        ModRegistries.CUSTOM_DATA.register("tickets",() -> TicketDataCache.TYPE);
        ModRegistries.CUSTOM_DATA.register("event_rewards",() -> EventRewardDataCache.TYPE);
    }

}
