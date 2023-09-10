package io.github.lightman314.lightmanscurrency.common.menus.tax_collector;

import io.github.lightman314.lightmanscurrency.common.menus.TaxCollectorMenu;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public abstract class TaxCollectorTab implements IClientTracker {

    @Override
    public boolean isClient() { return this.menu.isClient(); }
    public final TaxCollectorMenu menu;
    public final TaxEntry getEntry() { return this.menu.getEntry(); }
    public final boolean hasAccess() { return this.menu.hasAccess(); }
    public final boolean isAdmin() { return this.menu.isAdmin(); }
    public final boolean isOwner() { return this.menu.isOwner(); }
    public final boolean isServerEntry() { return this.menu.isServerEntry(); }
    protected TaxCollectorTab(TaxCollectorMenu menu) { this.menu = menu; }

    public abstract Object createClientTab(Object screen);

    public boolean canBeAccessed() { return true; }

    /**
     * Called when the tab is opened. Use this to unhide slots.
     */
    public abstract void onTabOpen();

    /**
     * Called when the tab is closed. Use this to hide slots.
     */
    public abstract void onTabClose();

    /**
     * Called when the menu is loaded to add any tab-specific slots.
     */
    public void addMenuSlots(Function<Slot,Slot> addSlot) {}

    public boolean quickMoveStack(ItemStack stack) { return false; }

    /**
     * Sends a message to the server to notify them about an interaction made client-side.
     */
    public abstract void receiveMessage(LazyPacketData message);

}