package io.github.lightman314.lightmanscurrency.api.trader.notifications.categories;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.notifications.category.NotificationCategoryType;
import io.github.lightman314.lightmanscurrency.api.notifications.category.SingletonNotificationCategory;

public final class TraderSettingsCategory extends SingletonNotificationCategory {

    public static final TraderSettingsCategory INSTANCE = new TraderSettingsCategory();
    public static final NotificationCategoryType<TraderSettingsCategory> TYPE = SingletonNotificationCategory.buildType(INSTANCE);

    private TraderSettingsCategory() {}

    @Override
    public IconData getIcon() { return SpriteIcon.of(LCApi.id("icon/settings")); }
    @Override
    public NotificationCategoryType<?> getType() { return TYPE; }

}