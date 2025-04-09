package io.github.lightman314.lightmanscurrency.api.traders.easy_data;

import io.github.lightman314.lightmanscurrency.api.easy_data.EasyDataSettings;
import io.github.lightman314.lightmanscurrency.api.easy_data.util.NotificationReplacer;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeCreativeNotification;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TraderNotificationReplacers {

    private TraderNotificationReplacers() {}

    public static final NotificationReplacer<Boolean> CREATIVE_NOTIFICATION = new CreativeNotificationReplacer();


    private static class CreativeNotificationReplacer implements NotificationReplacer<Boolean>
    {
        @Nullable
        @Override
        public Notification replaceNotification(Boolean oldValue, Boolean newValue, PlayerReference player, EasyDataSettings<Boolean> settings, @Nullable Notification originalNotification) { return new ChangeCreativeNotification(player,newValue); }
    }

}