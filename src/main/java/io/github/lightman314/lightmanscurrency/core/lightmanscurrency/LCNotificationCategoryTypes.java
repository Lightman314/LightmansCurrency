package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.bank_account.notifications.categories.BankCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.category.NotificationCategoryType;
import io.github.lightman314.lightmanscurrency.api.notifications.category.builtin.*;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.categories.*;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCNotificationCategoryTypes {
    private LCNotificationCategoryTypes() {}

    public static final DeferredRegister<NotificationCategoryType<?>> REGISTER = DeferredRegister.create(LCRegistries.Notifications.NOTIFICATION_CATEGORY_TYPE,LCApi.MODID);

    static {
        register("general",GeneralCategory.TYPE);
        register("system",SystemCategory.TYPE);
        register("trader",TraderCategory.TYPE);
        register("trader_settings",TraderSettingsCategory.TYPE);
        register("bank",BankCategory.TYPE);
    }

    private static void register(String name,NotificationCategoryType<?> type) {
        REGISTER.register(name,() -> type);
    }

}