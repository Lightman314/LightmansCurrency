package io.github.lightman314.lightmanscurrency.common.menus.traderstorage;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.TraderStatsClientTab;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

public class TraderStatsTab extends TraderStorageTab {

    public TraderStatsTab(@Nonnull ITraderStorageMenu menu) { super(menu); }

    @Nonnull
    @Override
    public Object createClientTab(@Nonnull Object screen) { return new TraderStatsClientTab(screen,this); }
    @Override
    public boolean canOpen(Player player) { return this.menu.hasPermission(Permissions.VIEW_LOGS); }

    public void clearStats(boolean fullClear)
    {
        if(this.menu.hasPermission(Permissions.EDIT_SETTINGS))
        {
            if(this.menu.isClient())
            {
                this.menu.SendMessage(this.builder().setBoolean("ClearStats",fullClear));
            }
            else
            {
                TraderData trader = this.menu.getTrader();
                if(trader != null)
                {
                    trader.statTracker.clear(fullClear);
                    trader.markStatsDirty();
                }
            }
        }
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("ClearStats"))
            this.clearStats(message.getBoolean("ClearStats"));
    }

}
