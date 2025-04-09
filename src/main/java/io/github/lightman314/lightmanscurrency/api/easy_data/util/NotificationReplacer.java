package io.github.lightman314.lightmanscurrency.api.easy_data.util;

import io.github.lightman314.lightmanscurrency.api.easy_data.EasyDataSettings;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface NotificationReplacer<T> {

    static <T> NotificationReplacer<T> empty() {
        return new NotificationReplacer<>() {
            @Nullable
            @Override
            public Notification replaceNotification(T oldValue, T newValue, PlayerReference player, EasyDataSettings<T> settings, @Nullable Notification originalNotification) { return originalNotification; }
        };
    }

    @Nullable
    Notification replaceNotification(T oldValue, T newValue, PlayerReference player, EasyDataSettings<T> settings, @Nullable Notification originalNotification);

}
