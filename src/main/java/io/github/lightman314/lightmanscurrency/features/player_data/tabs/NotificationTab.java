package io.github.lightman314.lightmanscurrency.features.player_data.tabs;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationStack;
import io.github.lightman314.lightmanscurrency.api.notifications.category.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.holder.NotificationReader;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import io.github.lightman314.lightmanscurrency.features.api_impl.data.PlayerNotificationCache;
import io.github.lightman314.lightmanscurrency.features.player_data.PlayerDataMenu;
import io.github.lightman314.lightmanscurrency.features.player_data.PlayerDataTab;
import net.minecraft.resources.Identifier;

import java.util.List;

public class NotificationTab extends PlayerDataTab {

    public static final Identifier CLIENT_KEY = LCApi.id("notifications");

    public static final TextEntry NAME = TextEntry.tooltip(LCApi.MODID,"player_data.notifications");
    public static final TextEntry BUTTON_MARK_AS_READ = TextEntry.button(LCApi.MODID,"player_data.notifications.mark_as_read");

    public NotificationTab(PlayerDataMenu menu) { super(menu); }

    @Override
    public Identifier getClientTabKey() { return CLIENT_KEY; }

    public NotificationReader getNotificationReader() { return LCApi.getNotificationAPI().getPlayerNotifications(this.getPlayer()); }
    public List<NotificationStack> getNotifications(NotificationCategory category) { return this.getNotificationReader().getNotifications(category); }

    public void deleteNotification(NotificationCategory category,int index) {
        if(this.isClient()) {
            this.send(FancyPacketMap.map().setMap("deleteNotification",FancyPacketMap.map()
                    .set("category",LCFancyPacketTypes.NOTIFICATION_CATEGORY,category)
                    .setInt("index",index)));
        }
        PlayerNotificationCache data = PlayerNotificationCache.TYPE.get(this);
        if(data != null)
            data.deleteNotification(this.getPlayer(),category,index);
    }

    public void markAsRead(NotificationCategory category) {
        if(this.isClient()) {
            this.send(FancyPacketMap.map()
                    .set("markAsRead",LCFancyPacketTypes.NOTIFICATION_CATEGORY,category));
        }
        PlayerNotificationCache data = PlayerNotificationCache.TYPE.get(this);
        if(data != null)
            data.markAsRead(this.getPlayer(),category);
    }

    @Override
    public void handleMessage(FancyPacketMap message) {
        if(message.contains("deleteNotification")) {
            FancyPacketMap entry = message.getMap("deleteNotification");
            NotificationCategory category = entry.get("category",LCFancyPacketTypes.NOTIFICATION_CATEGORY);
            int index = entry.getInt("index");
            if(category != null)
                this.deleteNotification(category,index);
        }
        if(message.contains("markAsRead")) {
            NotificationCategory category = message.get("markAsRead",LCFancyPacketTypes.NOTIFICATION_CATEGORY);
            if(category != null)
                this.markAsRead(category);
        }
    }

}