package io.github.lightman314.lightmanscurrency.common.data.types;

import io.github.lightman314.lightmanscurrency.api.events.NotificationEvent;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomData;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomDataType;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.common.util.LookupHelper;
import io.github.lightman314.lightmanscurrency.network.message.notifications.SPacketChatNotification;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class NotificationDataCache extends CustomData {

    public static final CustomDataType<NotificationDataCache> TYPE = new CustomDataType<>("lightmanscurrency_notification_data",NotificationDataCache::new);

    private final Map<UUID, NotificationData> playerNotifications = new HashMap<>();

    private NotificationDataCache() {}

    @Override
    public CustomDataType<?> getType() { return TYPE; }

    @Override
    public void save(CompoundTag tag, HolderLookup.Provider lookup) {
        ListTag notificationData = new ListTag();
        this.playerNotifications.forEach((id,data) -> {
            CompoundTag entry = data.save(lookup);
            entry.putUUID("Player", id);
            notificationData.add(entry);
        });
        tag.put("PlayerNotifications", notificationData);
    }

    @Override
    protected void load(CompoundTag tag, HolderLookup.Provider lookup) {
        ListTag notificationData = tag.getList("PlayerNotifications", Tag.TAG_COMPOUND);
        for(int i = 0; i < notificationData.size(); ++i)
        {
            CompoundTag entry = notificationData.getCompound(i);
            UUID id = entry.getUUID("Player");
            NotificationData data = NotificationData.loadFrom(entry,lookup);
            if(id != null && data != null)
                this.playerNotifications.put(id, data);
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
        if(this.isServer() && this.playerNotifications.containsKey(player))
        {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if(server == null)
                return;
            ServerPlayer sp = server.getPlayerList().getPlayer(player);
            if(sp == null)
                return;
            this.syncNotifications(sp);
        }
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
        this.markNotificationsDirty(player);
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

    private void syncNotifications(ServerPlayer player)
    {
        this.sendSyncPacket(this.builder().setCompound("UpdateNotifications",this.getNotifications(player).save(LookupHelper.getRegistryAccess())).setUUID("Player",player.getUUID()),player);
    }

    @Override
    protected void parseSyncPacket(LazyPacketData message, HolderLookup.Provider lookup) {
        if(message.contains("UpdateNotifications"))
        {
            NotificationData data = NotificationData.loadFrom(message.getNBT("UpdateNotifications"),lookup);
            UUID player = message.getUUID("Player");
            this.playerNotifications.put(player,data);
        }
    }

    @Override
    public void onPlayerJoin(ServerPlayer player) {
        this.syncNotifications(player);
    }

}
