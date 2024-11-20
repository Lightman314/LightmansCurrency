package io.github.lightman314.lightmanscurrency.common.menus.tax_collector;

import io.github.lightman314.lightmanscurrency.common.menus.TaxCollectorMenu;
import io.github.lightman314.lightmanscurrency.common.menus.tabbed.EasyMenuTab;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

public abstract class TaxCollectorTab extends EasyMenuTab<TaxCollectorMenu,TaxCollectorTab> {

    public final TaxEntry getEntry() { return this.menu.getEntry(); }
    public final boolean hasAccess() { return this.menu.hasAccess(); }
    public final boolean isAdmin() { return this.menu.isAdmin(); }
    public final boolean isOwner() { return this.menu.isOwner(); }
    public final boolean isServerEntry() { return this.menu.isServerEntry(); }
    protected TaxCollectorTab(TaxCollectorMenu menu) { super(menu); }

    public boolean canOpen(@Nonnull Player player) { return true; }

}