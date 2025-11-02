package io.github.lightman314.lightmanscurrency.api.notifications;

import io.github.lightman314.lightmanscurrency.common.impl.NotificationAPIImpl;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class NotificationAPI {

    private static NotificationAPI instance;
    public static NotificationAPI getApi()
    {
        if(instance == null)
            instance = new NotificationAPIImpl();
        return instance;
    }

    protected NotificationAPI() { if(instance != null)  throw new IllegalCallerException("Cannot create a new NotificationAPI instance as one is already present!"); }

    /**
     * Registers the given {@link NotificationType} to the registry so that it can be loaded via {@link #LoadNotification(CompoundTag)}
     */
    public abstract void RegisterNotification(NotificationType<?> type);

    /**
     * Registers the given {@link NotificationCategoryType} to the registry so that it can be loaded via {@link #LoadCategory(CompoundTag)}
     */
    public abstract void RegisterCategory(NotificationCategoryType<?> type);

    /**
     * Attempts to load a notification from the given NBT tag<br>
     * Lookup Provider is required in 1.21+ as it's used to save text components for some odd reason<br>
     * Should only attempt to load tags saved by {@link Notification#save()}
     */
    @Nullable
    public abstract Notification LoadNotification(CompoundTag tag);

    /**
     * Attempts to load the category from the given NBT tag<br>
     * Lookup Provider is required in 1.21+ as it's used to save text components for some odd reason<br>
     * Should only attempt to load tags saved by {@link NotificationCategory#save()}
     */
    @Nullable
    public abstract NotificationCategory LoadCategory(CompoundTag tag);

    public final void PushPlayerNotification(UUID playerID, Notification notification) { this.PushPlayerNotification(playerID,notification,true); }
    public abstract void PushPlayerNotification(UUID playerID, Notification notification, boolean pushToChat);

}