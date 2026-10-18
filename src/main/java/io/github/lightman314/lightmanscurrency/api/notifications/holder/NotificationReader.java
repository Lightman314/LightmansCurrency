package io.github.lightman314.lightmanscurrency.api.notifications.holder;

import io.github.lightman314.lightmanscurrency.api.notifications.NotificationStack;
import io.github.lightman314.lightmanscurrency.api.notifications.category.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.category.builtin.UnfilteredCategory;

import java.util.List;

public interface NotificationReader {

    List<NotificationStack> getNotifications();
    default List<NotificationStack> getNotifications(NotificationCategory category) {
        if(category == UnfilteredCategory.INSTANCE)
            return this.getNotifications();
        return this.getNotifications(NotificationFilter.ofCategory(category));
    }
    List<NotificationStack> getNotifications(NotificationFilter filter);
    List<NotificationCategory> getCategories();
    boolean hasUnseenNotification();
    boolean hasUnseenNotification(NotificationCategory category);
    boolean hasUnseenNotification(NotificationFilter filter);

}
