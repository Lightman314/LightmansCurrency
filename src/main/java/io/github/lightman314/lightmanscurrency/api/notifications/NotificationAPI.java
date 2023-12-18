package io.github.lightman314.lightmanscurrency.api.notifications;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationSaveData;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NotificationAPI {

    private static final Map<String,NotificationType<?>> notificationRegistry = new HashMap<>();
    private static final Map<String,NotificationCategoryType<?>> categoryRegistry = new HashMap<>();


    public static void registerNotification(@Nonnull NotificationType<?> type)
    {
        String t = type.type.toString();
        if(notificationRegistry.containsKey(t))
        {
            LightmansCurrency.LogWarning("Attempted to register duplicate NotificationType '" + t + "'!");
            return;
        }
        notificationRegistry.put(t, type);
        LightmansCurrency.LogInfo("Registered NotificationType " + type);
    }

    public static void registerCategory(@Nonnull NotificationCategoryType<?> type)
    {
        String t = type.type.toString();
        if(categoryRegistry.containsKey(t))
        {
            LightmansCurrency.LogWarning("Attempted to register duplicate NotificationCategoryType '" + t + "'!");
            return;
        }
        categoryRegistry.put(t, type);
        LightmansCurrency.LogInfo("Registered NotificationCategoryType " + type);
    }

    @Nullable
    public static Notification loadNotification(@Nonnull CompoundTag compound) {
        if(compound.contains("Type") || compound.contains("type"))
        {
            String type = compound.getString("type");
            if(notificationRegistry.containsKey(type))
                return notificationRegistry.get(type).load(compound);
            else
            {
                LightmansCurrency.LogError("Cannot load notification type " + type + " as no NotificationType has been registered.");
                return null;
            }
        }
        else
        {
            LightmansCurrency.LogError("Cannot deserialize notification as tag is missing the 'type' tag.");
            return null;
        }
    }

    @Nullable
    public static NotificationCategory loadCategory(@Nonnull CompoundTag compound)
    {
        if(compound.contains("Type") || compound.contains("type"))
        {
            String type = compound.contains("Type") ? compound.getString("Type") : compound.getString("type");
            if(categoryRegistry.containsKey(type))
                return categoryRegistry.get(type).load(compound);
            else
            {
                LightmansCurrency.LogError("Cannot load notification category type " + type + " as no NotificationCategoryType has been registered.");
                return null;
            }
        }
        else
        {
            LightmansCurrency.LogError("Cannot deserialize notification category as tag is missing the 'type' tag.");
            return null;
        }
    }

    public static void PushPlayerNotification(@Nonnull UUID playerID, @Nonnull Notification notification) { NotificationSaveData.PushNotification(playerID, notification); }
    public static void PushPlayerNotification(@Nonnull UUID playerID, @Nonnull Notification notification, boolean pushToChat) { NotificationSaveData.PushNotification(playerID, notification, pushToChat); }


}
