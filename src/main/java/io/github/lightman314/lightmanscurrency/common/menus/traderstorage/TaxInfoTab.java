package io.github.lightman314.lightmanscurrency.common.menus.traderstorage;

import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.TaxInfoClientTab;
import io.github.lightman314.lightmanscurrency.common.data.types.TaxDataCache;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

public class TaxInfoTab extends TraderStorageTab {

    public TaxInfoTab(TraderStorageMenu menu) { super(menu); }

    @Nonnull
    @Override
    public Object createClientTab(@Nonnull Object screen) { return new TaxInfoClientTab(screen, this); }

    @Override
    public boolean canOpen(Player player) { return this.menu.hasPermission(Permissions.EDIT_SETTINGS); }

    public void AcceptTaxes(long taxCollector)
    {
        TraderData trader = this.menu.getTrader();
        if(trader != null && trader.hasPermission(this.menu.getPlayer(), Permissions.EDIT_SETTINGS))
        {
            TaxEntry entry = TaxDataCache.TYPE.get(this).getEntry(taxCollector);
            if(entry != null && entry.IsInArea(trader))
                entry.AcceptTaxable(trader);
            if(this.menu.isClient())
                this.menu.SendMessage(this.builder().setLong("AcceptTaxCollector", taxCollector));
        }
    }

    public void ForceIgnoreTaxCollector(long taxCollector)
    {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
        {
            TaxEntry entry = TaxDataCache.TYPE.get(this).getEntry(taxCollector);
            if(entry != null && entry.IsInArea(trader))
                trader.FlagTaxEntryToIgnore(entry, this.menu.getPlayer());
            if(this.menu.isClient())
                this.menu.SendMessage(this.builder().setLong("ForceIgnoreTaxCollector", taxCollector));
        }
    }

    public void PardonIgnoredTaxCollector(long taxCollector)
    {
        TraderData trader = this.menu.getTrader();
        if(trader != null && trader.hasPermission(this.menu.getPlayer(), Permissions.EDIT_SETTINGS))
        {
            TaxEntry entry = TaxDataCache.TYPE.get(this).getEntry(taxCollector);
            if(entry != null && entry.IsInArea(trader))
                trader.PardonTaxEntry(entry);
            if(this.menu.isClient())
                this.menu.SendMessage(this.builder().setLong("PardonTaxCollector", taxCollector));
        }
    }

    @Override
    public void receiveMessage(LazyPacketData message) {

        if(message.contains("AcceptTaxCollector"))
            this.AcceptTaxes(message.getLong("AcceptTaxCollector"));
        if(message.contains("ForceIgnoreTaxCollector"))
            this.ForceIgnoreTaxCollector(message.getLong("ForceIgnoreTaxCollector"));
        if(message.contains("PardonTaxCollector"))
            this.PardonIgnoredTaxCollector(message.getLong("PardonTaxCollector"));
    }

}