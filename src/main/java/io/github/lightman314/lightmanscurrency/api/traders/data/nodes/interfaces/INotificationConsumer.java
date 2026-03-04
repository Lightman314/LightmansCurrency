package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.notifications.Notification;

import java.util.function.Supplier;

public interface INotificationConsumer {

    void pushNotification(Supplier<Notification> source,int notificationLevel,boolean notificationsToChat);

}
