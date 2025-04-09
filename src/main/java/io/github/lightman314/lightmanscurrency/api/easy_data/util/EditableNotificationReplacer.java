package io.github.lightman314.lightmanscurrency.api.easy_data.util;

import io.github.lightman314.lightmanscurrency.api.easy_data.EasyDataSettings;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;

import javax.annotation.Nullable;

public interface EditableNotificationReplacer<T,A> {

    @Nullable
    Notification replaceNotification(T value, A field, PlayerReference player, EasyDataSettings<T> settings, @Nullable Notification original);

}
