package io.github.lightman314.lightmanscurrency.api.notifications;

import io.github.lightman314.lightmanscurrency.common.impl.NotificationAPIImpl;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public abstract class NotificationAPI {

    public static NotificationAPI API = NotificationAPIImpl.INSTANCE;

    /**
     * Registers the given {@link NotificationType} to the registry so that it can be loaded via {@link #LoadNotification(CompoundTag)}
     */
    public abstract void RegisterNotification(@Nonnull NotificationType<?> type);

    /**
     * @deprecated Use {@link #RegisterNotification(NotificationType)} instead
     * @see #API
     */
    @Deprecated(since = "2.2.3.2")
    public static void registerNotification(@Nonnull NotificationType<?> type) { API.RegisterNotification(type); }

    /**
     * Registers the given {@link NotificationCategoryType} to the registry so that it can be loaded via {@link #LoadCategory(CompoundTag)}
     */
    public abstract void RegisterCategory(@Nonnull NotificationCategoryType<?> type);

    /**
     * @deprecated Use {@link #RegisterCategory(NotificationCategoryType)} instead
     * @see #API
     */
    @Deprecated(since = "2.2.3.2")
    public static void registerCategory(@Nonnull NotificationCategoryType<?> type) { API.RegisterCategory(type); }

    /**
     * Attempts to load a notification from the given NBT tag<br>
     * Lookup Provider is required in 1.21+ as it's used to save text components for some odd reason<br>
     * Should only attempt to load tags saved by {@link Notification#save()}
     */
    @Nullable
    public abstract Notification LoadNotification(@Nonnull CompoundTag tag);

    /**
     * @deprecated Use {@link #LoadNotification(CompoundTag)} instead
     * @see #API
     */
    @Deprecated(since = "2.2.3.2")
    @Nullable
    public static Notification loadNotification(@Nonnull CompoundTag compound) { return API.LoadNotification(compound); }

    /**
     * Attempts to load the category from the given NBT tag<br>
     * Lookup Provider is required in 1.21+ as it's used to save text components for some odd reason<br>
     * Should only attempt to load tags saved by {@link NotificationCategory#save()}
     */
    @Nullable
    public abstract NotificationCategory LoadCategory(@Nonnull CompoundTag tag);

    /**
     * @deprecated Use {@link #LoadCategory(CompoundTag)} instead
     * @see #API
     */
    @Deprecated(since = "2.2.3.2")
    @Nullable
    public static NotificationCategory loadCategory(@Nonnull CompoundTag compound) { return API.LoadCategory(compound); }

    public final void PushPlayerNotification(@Nonnull UUID playerID, @Nonnull Notification notification) { this.PushPlayerNotification(playerID,notification,true); }
    public abstract void PushPlayerNotification(@Nonnull UUID playerID, @Nonnull Notification notification, boolean pushToChat);

}