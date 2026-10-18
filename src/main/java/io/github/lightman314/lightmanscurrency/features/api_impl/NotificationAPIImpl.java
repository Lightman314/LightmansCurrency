package io.github.lightman314.lightmanscurrency.features.api_impl;

import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationAPI;
import io.github.lightman314.lightmanscurrency.api.notifications.holder.NotificationReader;
import io.github.lightman314.lightmanscurrency.features.api_impl.data.PlayerNotificationCache;
import io.github.lightman314.lightmanscurrency.network.message.notifications.SPacketChatNotification;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.UUID;

public final class NotificationAPIImpl implements NotificationAPI {
    private NotificationAPIImpl() {}

    public static final NotificationAPIImpl INSTANCE = new NotificationAPIImpl();

    @Override
    public NotificationReader getPlayerNotifications(Player player) { return PlayerNotificationCache.TYPE.get(ISidedContext.wrap(player)).getNotifications(player); }

    @Override
    public long startTrackingPlayerNotifications(Player tracking,UUID playerID) {
        ISidedContext context = ISidedContext.wrap(tracking);
        if(context.isClient())
            return -1;
        return PlayerNotificationCache.TYPE.get(context).startTracking(tracking,playerID);
    }

    @Override
    public void stopTrackingPlayerNotifications(long trackingKey) {
        if(trackingKey < 0)
            return;
        PlayerNotificationCache data = PlayerNotificationCache.TYPE.get(ISidedContext.LOGICAL_SERVER);
        if(data != null)
            data.stopTracking(trackingKey);
    }

    @Override
    public void pushPlayerNotification(UUID playerID,Notification notification,boolean pushToChat) {
        PlayerNotificationCache data = PlayerNotificationCache.TYPE.get(ISidedContext.LOGICAL_SERVER);
        if(data != null) {
            data.postNotification(playerID,notification);
            if(pushToChat) {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                if(server != null) {
                    Player player = PlayerReference.of(playerID,"").getPlayer();
                    if(player != null)
                        new SPacketChatNotification(notification).sendTo(player);
                }
            }
        }
    }

}