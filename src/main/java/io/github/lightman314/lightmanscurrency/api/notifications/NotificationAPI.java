package io.github.lightman314.lightmanscurrency.api.notifications;

import io.github.lightman314.lightmanscurrency.common.impl.NotificationAPIImpl;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class NotificationAPI {

    @Nullable
    private static NotificationAPI instance;
    public static NotificationAPI getApi()
    {
        if(instance == null)
            instance = new NotificationAPIImpl();
        return instance;
    }

    protected NotificationAPI() { if(instance != null) throw new IllegalCallerException("Cannot create a new NotificationAPI instance as one is already present!"); }

    public final void PushPlayerNotification(UUID playerID, Notification notification) { this.PushPlayerNotification(playerID,notification,true); }
    public abstract void PushPlayerNotification(UUID playerID, Notification notification, boolean pushToChat);

}
