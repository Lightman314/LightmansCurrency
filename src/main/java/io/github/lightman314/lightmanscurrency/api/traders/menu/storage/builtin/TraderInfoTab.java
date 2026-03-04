package io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.info.TraderInfoClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class TraderInfoTab extends TraderStorageTab {

    public static final ResourceLocation KEY = LightmansCurrency.id("info");

    public TraderInfoTab(ITraderStorageMenu menu) { super(menu); }

    @Override
    public ResourceLocation tabKey() { return KEY; }

    @Override
    public Object createClientTab(Object screen) { return new TraderInfoClientTab(screen,this); }

    @Override
    public boolean canOpen(Player player) { return this.menu.hasPermission(Permissions.VIEW_LOGS) || this.menu.hasPermission(Permissions.EDIT_SETTINGS); }

    @Override
    public void receiveMessage(LazyPacketData message) {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
            trader.handleSettingsChange(this.menu.getPlayer(),message);
    }

}
