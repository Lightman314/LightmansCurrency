package io.github.lightman314.lightmanscurrency.common.menus;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.notifications.*;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.data.types.NotificationDataCache;
import io.github.lightman314.lightmanscurrency.common.menus.providers.EasyMenuProvider;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class NotificationMenu extends LazyMessageMenu {

    public static final MenuProvider PROVIDER = new Provider();

    public NotificationMenu(int id, Inventory inventory) { super(ModMenus.NOTIFICATIONS.get(), id, inventory); }

    @Override
    public void processMessage(LazyPacketData message) {
        if(message.contains("MarkAsRead"))
        {
            NotificationCategory category = message.decodeObject("MarkAsRead",NotificationCategory.CODEC);
            if(category == null)
                return;
            NotificationDataCache d = NotificationDataCache.TYPE.get(false);
            if(d == null)
                return;
            NotificationData data = d.getNotifications(this.player);
            if(data != null && data.unseenNotification(category))
            {
                for(Notification n : data.getNotifications(category))
                {
                    if(!n.wasSeen())
                        n.setSeen();
                }
                d.markNotificationsDirty(this.player.getUUID());
            }
        }
        if(message.contains("DeleteNotification"))
        {
            NotificationCategory category = message.decodeObject("Category",NotificationCategory.CODEC);
            if(category == null)
                return;
            NotificationDataCache d = NotificationDataCache.TYPE.get(false);
            if(d == null)
                return;
            NotificationData data = d.getNotifications(this.player);
            if(data != null)
            {
                data.deleteNotification(category,message.getInt("DeleteNotification"));
                d.markNotificationsDirty(this.player.getUUID());
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }

    private static class Provider implements EasyMenuProvider {
        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int menuID, Inventory inventory, Player player) { return new NotificationMenu(menuID,inventory); }
    }

}
