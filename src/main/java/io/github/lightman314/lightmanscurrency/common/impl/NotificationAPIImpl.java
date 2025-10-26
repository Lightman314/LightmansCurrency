package io.github.lightman314.lightmanscurrency.common.impl;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.notifications.*;
import io.github.lightman314.lightmanscurrency.common.data.types.NotificationDataCache;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NotificationAPIImpl extends NotificationAPI {

    public NotificationAPIImpl() {}

    private final Map<String, NotificationType<?>> notificationRegistry = new HashMap<>();
    private final Map<String, NotificationCategoryType<?>> categoryRegistry = new HashMap<>();

    @Override
    public void RegisterNotification(NotificationType<?> type) {
        String t = type.type.toString();
        if(this.notificationRegistry.containsKey(t))
        {
            LightmansCurrency.LogWarning("Attempted to registerType duplicate NotificationType '" + t + "'!");
            return;
        }
        this.notificationRegistry.put(t, type);
        LightmansCurrency.LogInfo("Registered NotificationType " + type);
    }

    @Override
    public void RegisterCategory(NotificationCategoryType<?> type) {
        String t = type.type.toString();
        if(categoryRegistry.containsKey(t))
        {
            LightmansCurrency.LogWarning("Attempted to registerType duplicate NotificationCategoryType '" + t + "'!");
            return;
        }
        categoryRegistry.put(t, type);
        LightmansCurrency.LogInfo("Registered NotificationCategoryType " + type);
    }

    @Nullable
    @Override
    public Notification LoadNotification(CompoundTag tag, HolderLookup.Provider lookup) {
        if(tag.contains("Type") || tag.contains("type"))
        {
            String type = tag.contains("Type") ? tag.getString("Type") : tag.getString("type");
            if(this.notificationRegistry.containsKey(type))
            {
                try {
                    return this.notificationRegistry.get(type).load(tag, lookup);
                } catch (Throwable t) {
                    LightmansCurrency.LogError("Error loading Notification of type '" + type + "'", t);
                    return null;
                }
            }
            else
            {
                LightmansCurrency.LogError("Cannot load notification type " + type + " as no NotificationType has been registered with that name");
                return null;
            }
        }
        else
        {
            LightmansCurrency.LogError("Cannot deserialize notification as tag is missing the 'type' tag");
            return null;
        }
    }

    @Nullable
    @Override
    public NotificationCategory LoadCategory(CompoundTag tag, HolderLookup.Provider lookup) {
        if(tag.contains("Type") || tag.contains("type"))
        {
            String type = tag.contains("Type") ? tag.getString("Type") : tag.getString("type");
            if(categoryRegistry.containsKey(type))
                return categoryRegistry.get(type).load(tag, lookup);
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

    @Override
    public void PushPlayerNotification(UUID playerID, Notification notification, boolean pushToChat) { NotificationDataCache.TYPE.get(false).pushNotification(playerID,notification,pushToChat); }

}
