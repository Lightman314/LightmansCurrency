package io.github.lightman314.lightmanscurrency.common.core.custom;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomDataType;
import io.github.lightman314.lightmanscurrency.common.data.types.*;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCustomDataTypes {

    public static final DeferredRegister<CustomDataType<?>> REGISTER = DeferredRegister.create(LCRegistries.CUSTOM_DATA,LightmansCurrency.MODID);

    static {
        REGISTER.register("trader",() -> TraderDataCache.TYPE);
        REGISTER.register("bank_accounts",() -> BankDataCache.TYPE);
        REGISTER.register("teams",() -> TeamDataCache.TYPE);
        REGISTER.register("notifications",() -> NotificationDataCache.TYPE);
        REGISTER.register("ejection_data",() -> EjectionDataCache.TYPE);
        REGISTER.register("tax_entries",() -> TaxDataCache.TYPE);
        REGISTER.register("tickets",() -> TicketDataCache.TYPE);
        REGISTER.register("event_rewards",() -> EventRewardDataCache.TYPE);
    }

}
