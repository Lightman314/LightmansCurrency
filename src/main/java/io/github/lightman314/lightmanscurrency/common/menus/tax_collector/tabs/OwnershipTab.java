package io.github.lightman314.lightmanscurrency.common.menus.tax_collector.tabs;

import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.PlayerOwner;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.tax_collector.OwnershipClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TaxCollectorMenu;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.TaxCollectorTab;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;

import javax.annotation.Nonnull;

public class OwnershipTab extends TaxCollectorTab {

    public OwnershipTab(TaxCollectorMenu menu) { super(menu); }

    @Override
    public boolean canBeAccessed() { return this.isOwner() && !this.isServerEntry(); }

    @Override
    public Object createClientTab(Object screen) { return new OwnershipClientTab(screen, this); }

    @Override
    public void onTabOpen() { }

    @Override
    public void onTabClose() { }

    public void SetOwner(@Nonnull Owner newOwner)
    {
        TaxEntry entry = this.getEntry();
        if(entry != null && this.menu.isOwner())
        {
            entry.getOwner().SetOwner(newOwner);
            if(this.isClient())
                this.menu.SendMessageToServer(this.builder().setOwner("SetOwner", newOwner));
        }
    }

    public void SetOwnerPlayer(String playerName)
    {
        TaxEntry entry = this.getEntry();
        if(entry != null && this.menu.isOwner())
        {
            if(this.isServer())
            {
                PlayerReference newOwner = PlayerReference.of(false, playerName);
                if(newOwner != null)
                    entry.getOwner().SetOwner(PlayerOwner.of(newOwner));
            }
            else
                this.menu.SendMessageToServer(this.builder().setString("SetOwnerPlayer", playerName));
        }
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("SetOwner"))
            this.SetOwner(message.getOwner("SetOwner"));
        if(message.contains("SetOwnerPlayer"))
            this.SetOwnerPlayer(message.getString("SetOwnerPlayer"));
    }
}
