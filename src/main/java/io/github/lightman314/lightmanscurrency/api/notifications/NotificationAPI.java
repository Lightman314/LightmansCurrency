package io.github.lightman314.lightmanscurrency.api.notifications;

import java.util.UUID;

public interface NotificationAPI {

    default void pushPlayerNotification(UUID playerID,Notification notification) { this.pushPlayerNotification(playerID,notification,true); }
    void pushPlayerNotification(UUID playerID,Notification notification,boolean pushToChat);

}