package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.logs;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.logs.TraderLogClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Function;

public class TraderLogTab extends TraderStorageTab {

    public TraderLogTab(TraderStorageMenu menu) { super(menu); }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Object createClientTab(Object screen) { return new TraderLogClientTab(screen, this); }

    @Override
    public boolean canOpen(Player player) { return this.menu.hasPermission(Permissions.VIEW_LOGS); }

    @Override
    public void onTabOpen() { }

    @Override
    public void onTabClose() { }

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
    public void addStorageMenuSlots(Function<Slot, Slot> addSlot) { }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("DeleteNotification"))
            this.DeleteNotification(message.getInt("DeleteNotification"));
    }

}
