package io.github.lightman314.lightmanscurrency.api.notifications.holder;

import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.ownership.MemberLevel;

public interface NotificationConsumer extends ISidedContext {

    void postNotification(Notification notification);

    interface ForChat extends NotificationConsumer {

        @Override
        default void postNotification(Notification notification) { this.postNotification(notification,true); }
        void postNotification(Notification notification, boolean pushToChat);

    }

    interface ForMembers extends ForChat {
        @Override
        default void postNotification(Notification notification, boolean pushToChat) { this.postNotification(notification,MemberLevel.MEMBERS,pushToChat); }
        void postNotification(Notification notification, MemberLevel targets, boolean pushToChat);
    }

}