package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.core;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.info.TraderInfoClientTab;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class TraderInfoTab extends TraderStorageTab {

    public TraderInfoTab(@Nonnull ITraderStorageMenu menu) { super(menu); }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public Object createClientTab(@Nonnull Object screen) { return new TraderInfoClientTab(screen,this); }

    @Override
    public boolean canOpen(Player player) { return this.menu.hasPermission(Permissions.VIEW_LOGS) || this.menu.hasPermission(Permissions.EDIT_SETTINGS); }

    @Override
    public void receiveMessage(LazyPacketData message) {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
            trader.handleSettingsChange(this.menu.getPlayer(),message);
    }

}
