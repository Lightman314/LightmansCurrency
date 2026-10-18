package io.github.lightman314.lightmanscurrency.features.api_impl.data;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.data.FancyData;
import io.github.lightman314.lightmanscurrency.api.data.FancyDataType;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.tracking.UUIDTrackingSet;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.category.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.holder.NotificationHolder;
import io.github.lightman314.lightmanscurrency.api.notifications.holder.NotificationReader;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerNotificationCache extends FancyData {

    private static final Codec<PlayerNotificationCache> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC,NotificationHolder.CODEC).xmap(PlayerNotificationCache::new, d -> d.data);

    public static final FancyDataType<PlayerNotificationCache> TYPE = new FancyDataType<>("player_notifications", PlayerNotificationCache::new,CODEC);

    private final Map<UUID,NotificationHolder> data = new HashMap<>();
    private final UUIDTrackingSet tracking = new UUIDTrackingSet();

    private PlayerNotificationCache() {}
    private PlayerNotificationCache(Map<UUID,NotificationHolder> data) {
        data.forEach((id,h) -> this.data.put(id,h.setSidedContext(this)));
    }

    private Map<UUID,FancyPacketMap.Mutable> pendingPackets = new HashMap<>();

    @Override
    public FancyDataType<?> getType() {
        return TYPE;
    }

    public NotificationReader getNotifications(Player player) {
        UUID id = PlayerReference.of(player).id;
        return this.data.getOrDefault(id,new NotificationHolder());
    }

    public long startTracking(Player tracking,UUID playerID) {
        if(this.isClient())
            return -1;
        UUID trackerID = tracking.getUUID();
        var result = this.tracking.requestTracking(playerID,trackerID);
        if(result.sendFreshPacket() && this.data.containsKey(playerID)) {
            NotificationHolder holder = this.data.get(playerID);
            this.sendPacket(tracking,FancyPacketMap.map()
                    .setUUID("player",playerID)
                    .setList("notifications",LCFancyPacketTypes.NOTIFICATION_STACK,holder.getNotifications()));
        }
        return result.trackingKey();
    }

    public void stopTracking(long trackingKey) {
        if(this.isClient())
            return;
        this.tracking.endTracking(trackingKey);
    }

    public void postNotification(UUID playerID,Notification notification) {
        NotificationHolder holder = this.data.computeIfAbsent(playerID,i -> new NotificationHolder().setSidedContext(this));
        holder.postNotification(notification);
        this.setChanged(playerID,p -> p.addToList("addNotification",LCFancyPacketTypes.NOTIFICATION,notification));
    }

    public void deleteNotification(Player player,NotificationCategory category,int index) {
        UUID id = PlayerReference.of(player).id;
        if(this.data.containsKey(id)) {
            NotificationHolder holder = this.data.get(id);
            int result = holder.deleteNotification(category,index);
            if(result >= 0) {
                this.setChanged(id,p -> p.setList("notifications",LCFancyPacketTypes.NOTIFICATION_STACK,holder.getNotifications())
                        .remove("addNotification")); //Clear any previous additions as they'll be included in the full-list override
            }
        }
    }

    public void markAsRead(Player player, NotificationCategory category) {
        UUID id = PlayerReference.of(player).id;
        if(this.data.containsKey(id)) {
            NotificationHolder holder = this.data.get(id);
            if(holder.hasUnseenNotification(category)) {
                holder.markAsSeen(category);
                this.setChanged(id,p -> p.addToList("markAsRead",LCFancyPacketTypes.NOTIFICATION_CATEGORY,category));
            }
        }
    }

    private void setChanged(UUID id,Consumer<FancyPacketMap.Mutable> change) {
        if(this.isClient())
            return;
        this.setChanged();
        FancyPacketMap.Mutable entry = this.pendingPackets.computeIfAbsent(id,i -> FancyPacketMap.map().setUUID("player",id));
        change.accept(entry);
        this.tracking.setChanged(id);
    }

    //No auto-syncing, only send full notification packets to players when they request them
    @Override
    public void onPlayerJoin(ServerPlayer player) {
        //Player always tracks their own notifications
        this.startTracking(player,PlayerReference.of(player).id);
    }

    @Override
    public void syncTick() {
        Map<UUID,FancyPacketMap.Mutable> packets = this.pendingPackets;
        this.pendingPackets = new HashMap<>();
        MinecraftServer server = this.getServer();
        if(server != null) {
            packets.forEach((holderID,packet) -> {
                for(UUID trackingPlayer : this.tracking.getTrackingPlayers(holderID))
                    this.sendPacketToTarget(trackingPlayer,packet);
            });
        }
    }

    @Override
    public void onPlayerLeave(ServerPlayer player) {
        this.tracking.forgetPlayer(player);
        //Delete the tracking set for that players notifications if nobody else is tracking it at the current moment
        this.tracking.clearCacheIfEmpty(player.getUUID());
    }

    @Override
    protected void handleSyncPacket(FancyPacketMap data) {
        UUID player = data.getUUID("player");
        if(player != null) {
            NotificationHolder holder = this.data.computeIfAbsent(player,p -> new NotificationHolder().setSidedContext(this));
            if(data.contains("notifications"))
                holder.loadFrom(data.getList("notifications",LCFancyPacketTypes.NOTIFICATION_STACK));
            for(Notification n : data.getList("addNotification",LCFancyPacketTypes.NOTIFICATION))
                holder.addClientNotification(n);
            for(NotificationCategory category : data.getList("markAsRead",LCFancyPacketTypes.NOTIFICATION_CATEGORY))
                holder.markAsSeen(category);
        }
        else
            LightmansCurrency.LogWarning("Received a sync packet without the player id.");
    }

}