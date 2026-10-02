package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.ownership.MemberLevel;

public interface INotificationConsumerNode {

    void processNotification(Notification notification, boolean sendToMembers, MemberLevel targets, boolean pushToChat);

}
