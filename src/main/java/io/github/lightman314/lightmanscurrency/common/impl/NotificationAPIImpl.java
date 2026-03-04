package io.github.lightman314.lightmanscurrency.common.impl;

import io.github.lightman314.lightmanscurrency.api.notifications.*;
import io.github.lightman314.lightmanscurrency.common.data.types.NotificationDataCache;
import java.util.UUID;

public class NotificationAPIImpl extends NotificationAPI {

    public NotificationAPIImpl() {}

    @Override
    public void PushPlayerNotification(UUID playerID, Notification notification, boolean pushToChat) { NotificationDataCache.TYPE.get(false).pushNotification(playerID,notification,pushToChat); }

}
