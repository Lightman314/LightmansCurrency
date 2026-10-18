package io.github.lightman314.lightmanscurrency.api.notifications;

import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.notifications.holder.NotificationReader;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public interface NotificationAPI {

    NotificationReader getPlayerNotifications(Player player);

    default long startTrackingPlayerNotifications(Player tracking) { return this.startTrackingPlayerNotifications(tracking,PlayerReference.of(tracking));}
    default long startTrackingPlayerNotifications(Player tracking,PlayerReference player) { return this.startTrackingPlayerNotifications(tracking,player.id);}
    long startTrackingPlayerNotifications(Player tracking,UUID playerID);
    void stopTrackingPlayerNotifications(long trackingKey);

    default void pushPlayerNotification(UUID playerID,Notification notification) { this.pushPlayerNotification(playerID,notification,true); }
    void pushPlayerNotification(UUID playerID,Notification notification,boolean pushToChat);

}