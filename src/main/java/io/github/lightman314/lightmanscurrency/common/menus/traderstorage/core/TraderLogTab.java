package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.core;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.logs.TraderLogClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class TraderLogTab extends TraderStorageTab {

    public TraderLogTab(TraderStorageMenu menu) { super(menu); }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public Object createClientTab(@Nonnull Object screen) { return new TraderLogClientTab(screen, this); }

    @Override
    public boolean canOpen(Player player) { return this.menu.hasPermission(Permissions.VIEW_LOGS); }

    public boolean canDeleteNotification() { return this.menu.hasPermission(Permissions.TRANSFER_OWNERSHIP); }

    public void DeleteNotification(int notificationIndex)
    {
        if(this.menu.getTrader() != null && this.canDeleteNotification())
        {
            this.menu.getTrader().deleteNotification(this.menu.getPlayer(),notificationIndex);
            if(this.isClient())
                this.menu.SendMessage(this.builder().setInt("DeleteNotification",notificationIndex));
        }
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("DeleteNotification"))
            this.DeleteNotification(message.getInt("DeleteNotification"));
    }

}