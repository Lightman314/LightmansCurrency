package io.github.lightman314.lightmanscurrency.common.menus.tax_collector.tabs;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.tax_collector.AdminSettingsClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TaxCollectorMenu;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.TaxCollectorTab;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

public class AdminTab extends TaxCollectorTab {

    public AdminTab(TaxCollectorMenu menu) { super(menu); }

    @Override
    public boolean canOpen(@Nonnull Player player) { return this.isAdmin() && !this.isServerEntry(); }

    @Nonnull
    @Override
    public Object createClientTab(@Nonnull Object screen) { return new AdminSettingsClientTab(screen, this); }

    public void SetForceAcceptance(boolean newState)
    {
        TaxEntry entry = this.menu.getEntry();
        if(entry != null && this.hasAccess() && this.isAdmin())
        {
            entry.setForceAcceptance(newState);
            if(this.menu.isClient())
                this.menu.SendMessageToServer(this.builder().setBoolean("ChangeForceAcceptance", newState));
        }
    }

    public void SetInfiniteRange(boolean newState)
    {
        TaxEntry entry = this.menu.getEntry();
        if(entry != null && this.hasAccess() && this.isAdmin())
        {
            entry.setInfiniteRange(newState);
            if(this.menu.isClient())
                this.menu.SendMessageToServer(this.builder().setBoolean("ChangeInfiniteRange", newState));
        }
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("ChangeForceAcceptance"))
            this.SetForceAcceptance(message.getBoolean("ChangeForceAcceptance"));
        if(message.contains("ChangeInfiniteRange"))
            this.SetInfiniteRange(message.getBoolean("ChangeInfiniteRange"));
    }

}
