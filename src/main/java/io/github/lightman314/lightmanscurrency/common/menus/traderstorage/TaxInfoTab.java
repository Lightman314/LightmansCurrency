package io.github.lightman314.lightmanscurrency.common.menus.traderstorage;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.TaxInfoClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

import java.util.function.Function;

public class TaxInfoTab extends TraderStorageTab {

    public TaxInfoTab(TraderStorageMenu menu) { super(menu); }

    @Override
    public Object createClientTab(Object screen) { return new TaxInfoClientTab(screen, this); }

    @Override
    public boolean canOpen(Player player) { return this.menu.hasPermission(Permissions.EDIT_SETTINGS); }

    @Override
    public void onTabOpen() { }

    @Override
    public void onTabClose() { }

    @Override
    public void addStorageMenuSlots(Function<Slot, Slot> addSlot) { }

    public void AcceptTaxes(long taxCollector)
    {
        TraderData trader = this.menu.getTrader();
        if(trader != null && trader.hasPermission(this.menu.player, Permissions.EDIT_SETTINGS))
        {
            TaxEntry entry = TaxSaveData.GetTaxEntry(taxCollector, this.menu.isClient());
            if(entry != null && entry.IsInArea(trader))
                entry.acceptTaxes(trader);
            if(this.menu.isClient())
            {
                CompoundTag message = new CompoundTag();
                message.putLong("AcceptTaxCollector", taxCollector);
                this.menu.sendMessage(message);
            }
        }
    }

    public void ForceIgnoreTaxCollector(long taxCollector)
    {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
        {
            TaxEntry entry = TaxSaveData.GetTaxEntry(taxCollector, this.menu.isClient());
            if(entry != null && entry.IsInArea(trader))
                trader.FlagTaxEntryToIgnore(entry, this.menu.player);
            if(this.menu.isClient())
            {
                CompoundTag message = new CompoundTag();
                message.putLong("ForceIgnoreTaxCollector", taxCollector);
                this.menu.sendMessage(message);
            }
        }
    }

    public void PardonIgnoredTaxCollector(long taxCollector)
    {
        TraderData trader = this.menu.getTrader();
        if(trader != null && trader.hasPermission(this.menu.player, Permissions.EDIT_SETTINGS))
        {
            TaxEntry entry = TaxSaveData.GetTaxEntry(taxCollector, this.menu.isClient());
            if(entry != null && entry.IsInArea(trader))
                trader.PardonTaxEntry(entry);
            if(this.menu.isClient())
            {
                CompoundTag message = new CompoundTag();
                message.putLong("PardonTaxCollector", taxCollector);
                this.menu.sendMessage(message);
            }
        }
    }

    @Override
    public void receiveMessage(CompoundTag message) {

        if(message.contains("AcceptTaxCollector"))
            this.AcceptTaxes(message.getLong("AcceptTaxCollector"));
        if(message.contains("ForceIgnoreTaxCollector"))
            this.ForceIgnoreTaxCollector(message.getLong("ForceIgnoreTaxCollector"));
        if(message.contains("PardonTaxCollector"))
            this.PardonIgnoredTaxCollector(message.getLong("PardonTaxCollector"));
    }

}