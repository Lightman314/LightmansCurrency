package io.github.lightman314.lightmanscurrency.common.menus.tax_collector.tabs;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.tax_collector.ServerSettingsClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TaxCollectorMenu;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.TaxCollectorTab;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import net.minecraft.world.entity.player.Player;

public class ServerSettingsTab extends TaxCollectorTab {

    public ServerSettingsTab(TaxCollectorMenu menu) { super(menu); }

    @Override
    public Object createClientTab(Object screen) { return new ServerSettingsClientTab(screen,this); }

    @Override
    public boolean canOpen(Player player) { return this.menu.getEntry().isServerEntry(); }

    public void SetOnlyTargetsNetworkObjects(boolean newValue)
    {
        TaxEntry entry = this.menu.getEntry();
        if(entry != null && entry.isServerEntry() && entry.canAccess(this.menu.player))
        {
            entry.setOnlyTargetingNetwork(newValue);
            if(this.isClient())
                this.menu.SendMessageToServer(this.builder()
                        .setBoolean("ChangeOnlyTargetsNetwork",newValue));
        }
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("ChangeOnlyTargetsNetwork"))
            this.SetOnlyTargetsNetworkObjects(message.getBoolean("ChangeOnlyTargetsNetwork"));
    }

}