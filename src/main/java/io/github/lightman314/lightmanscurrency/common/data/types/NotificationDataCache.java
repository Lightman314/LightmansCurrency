package io.github.lightman314.lightmanscurrency.common.data.types;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.events.NotificationEvent;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomData;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomDataType;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.network.message.notifications.SPacketChatNotification;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NotificationDataCache extends CustomData {

    public static final CustomDataType<NotificationDataCache> TYPE = new CustomDataType<>("lightmanscurrency_notification_data",NotificationDataCache::new);
    private static final Codec<Map<UUID,NotificationData>> DATA_CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC,NotificationData.CODEC);

    private final Map<UUID, NotificationData> playerNotifications = new HashMap<>();

    private NotificationDataCache() {}

    @Override
    public CustomDataType<?> getType() { return TYPE; }

    @Override
    public void save(CompoundTag tag,DataContext<Tag> context) {
        tag.put("notifications",context.write(this.playerNotifications,DATA_CODEC));
    }

    @Override
    protected void load(CompoundTag tag,DataContext<Tag> context) {
        //Load old data
        if(tag.contains("PlayerNotifications"))
        {
            ListTag notificationData = tag.getList("PlayerNotifications", Tag.TAG_COMPOUND);
            for(int i = 0; i < notificationData.size(); ++i)
            {
                CompoundTag entry = notificationData.getCompound(i);
                UUID id = entry.getUUID("Player");
                NotificationData data = context.read(tag,NotificationData.CODEC);
                if(id != null && data != null)
                    this.playerNotifications.put(id,data);
            }
        }
        else
        {
            this.playerNotifications.clear();
            this.playerNotifications.putAll(context.safeReadMap(tag.get("notifications"),UUIDUtil.STRING_CODEC,NotificationData.CODEC,
                    s -> LightmansCurrency.LogError("Error loading Player Notification Data: " + s)));
        }
    }

    public NotificationData getNotifications(Player player) { return this.getNotifications(player.getUUID()); }

    public NotificationData getNotifications(UUID player)
    {
        if(!this.playerNotifications.containsKey(player))
        {
            this.playerNotifications.put(player, new NotificationData());
            this.setChanged();
        }
        return this.playerNotifications.get(player);
    }

    public void markNotificationsDirty(UUID player)
    {
        this.setChanged();
        {
            ServerPlayer sp = this.checkForPlayer(player);
            if(sp == null)
                return;
            this.fullSyncNotifications(sp);
        }
    }

    @Nullable
    private ServerPlayer checkForPlayer(UUID player)
    {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server == null)
            return null;
        return server.getPlayerList().getPlayer(player);
    }

    public void pushNotification(UUID player, Notification notification) { this.pushNotification(player,notification,true); }
    public void pushNotification(UUID player, Notification notification, boolean pushToChat) {
        //Post event to see if we should send the notification
        NotificationData data = this.getNotifications(player);
        NotificationEvent.NotificationSent.Pre event = new NotificationEvent.NotificationSent.Pre(player, data, notification);
        if(NeoForge.EVENT_BUS.post(event).isCanceled())
            return;
        //Passed the pre event, add the notification to the notification data
        data.addNotification(event.getNotification());
        //Mark the data as dirty
        this.setChanged();
        this.syncNotification(player,notification);
        //Run the post event to notify anyone who cares that the notification was created.
        NeoForge.EVENT_BUS.post(new NotificationEvent.NotificationSent.Post(player, data, event.getNotification()));

        //Send the notification message to the client so that it will be posted in chat
        if(pushToChat)
        {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if(server != null)
            {
                ServerPlayer sp = server.getPlayerList().getPlayer(player);
                if(sp != null)
                    new SPacketChatNotification(notification).sendTo(sp);
            }
        }
    }

    private void fullSyncNotifications(ServerPlayer player)
    {
        this.sendSyncPacket(this.builder()
                .setCustom("UpdateNotifications",this.getNotifications(player),ModLazyPackets.NOTIFICATION_DATA)
                .setUUID("Player",player.getUUID()),player);
    }

    private void syncNotification(UUID player,Notification notification) {

        ServerPlayer sp = this.checkForPlayer(player);
        if(sp != null)
        {
            this.sendSyncPacket(this.builder()
                    .setCustom("AddNotification",notification,ModLazyPackets.NOTIFICATION)
                    .setUUID("Player",player),
                    sp);
        }
    }

    @Override
    protected void parseSyncPacket(LazyPacketData message, HolderLookup.Provider lookup) {
        if(message.contains("UpdateNotifications"))
        {
            NotificationData data = message.getCustom("UpdateNotifications",ModLazyPackets.NOTIFICATION_DATA).flagAsClient(this);
            UUID player = message.getUUID("Player");
            this.playerNotifications.put(player,data);
        }
        if(message.contains("AddNotification"))
        {
            UUID player = message.getUUID("Player");
            Notification notification = message.getCustom("AddNotification",ModLazyPackets.NOTIFICATION);
            this.getNotifications(player).addNotification(notification.flagAsClient(this));
        }
    }

    @Override
    public void onPlayerJoin(ServerPlayer player) {
        this.fullSyncNotifications(player);
    }

}
