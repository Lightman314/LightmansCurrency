package io.github.lightman314.lightmanscurrency.api.trader.notifications;

import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.category.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.templates.SingleLineNotification;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.categories.TraderSettingsCategory;

public abstract class TraderSettingNotification extends Notification {

    @Override
    public final NotificationCategory getCategory() { return TraderSettingsCategory.INSTANCE; }

    public abstract static class SingleLine extends SingleLineNotification {
        @Override
        public final NotificationCategory getCategory() { return TraderSettingsCategory.INSTANCE; }
    }

}
