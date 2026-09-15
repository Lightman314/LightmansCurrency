package io.github.lightman314.lightmanscurrency.features.api_impl;

import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationAPI;

import java.util.UUID;

public final class NotificationAPIImpl implements NotificationAPI {
    private NotificationAPIImpl() {}

    public static final NotificationAPIImpl INSTANCE = new NotificationAPIImpl();

    @Override
    public void pushPlayerNotification(UUID playerID, Notification notification, boolean pushToChat) {
        //TODO re-implement player bank accounts
    }

}