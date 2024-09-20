package io.github.lightman314.lightmanscurrency.common.menus;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.notifications.*;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.menus.providers.EasyMenuProvider;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NotificationMenu extends LazyMessageMenu {

    public static final MenuProvider PROVIDER = new Provider();

    public NotificationMenu(int id, Inventory inventory) { super(ModMenus.NOTIFICATIONS.get(), id, inventory); }

    @Override
    public void HandleMessage(@Nonnull LazyPacketData message) {
        if(message.contains("MarkAsRead"))
        {
            NotificationCategory category = NotificationAPI.API.LoadCategory(message.getNBT("MarkAsRead"));
            if(category == null)
                return;
            NotificationData data = NotificationSaveData.GetNotifications(this.player);
            if(data != null && data.unseenNotification(category))
            {
                for(Notification n : data.getNotifications(category))
                {
                    if(!n.wasSeen())
                        n.setSeen();
                }
                NotificationSaveData.MarkNotificationsDirty(this.player.getUUID());
            }
        }
        if(message.contains("DeleteNotification"))
        {
            NotificationCategory category = NotificationAPI.API.LoadCategory(message.getNBT("Category"));
            if(category == null)
                return;
            NotificationData data = NotificationSaveData.GetNotifications(this.player);
            if(data != null)
            {
                data.deleteNotification(category,message.getInt("DeleteNotification"));
                NotificationSaveData.MarkNotificationsDirty(this.player.getUUID());
            }
        }
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int slot) { return ItemStack.EMPTY; }

    private static class Provider implements EasyMenuProvider {
        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int menuID, @Nonnull Inventory inventory, @Nonnull Player player) { return new NotificationMenu(menuID,inventory); }
    }

}