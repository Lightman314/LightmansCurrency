package io.github.lightman314.lightmanscurrency.common.menus.tabbed;

import io.github.lightman314.lightmanscurrency.api.network.IBuilderProvider;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public abstract class EasyMenuTab<M extends IEasyTabbedMenu<T>,T extends EasyMenuTab<M,T>> implements IBuilderProvider, IClientTracker {

    public final M menu;

    @Override
    public final boolean isClient() { return this.menu.isClient(); }

    public EasyMenuTab(M menu) { this.menu = menu; }

    public final HolderLookup.Provider registryAccess() { return this.menu.registryAccess(); }

    public abstract Object createClientTab(Object screen);

    /**
     * Whether the player has permission to access this tab.
     */
    public abstract boolean canOpen(Player player);

    /**
     * Called when the tab is opened. Use this to unhide slots.
     */
    public void onTabOpen() { }

    /**
     * Called when the tab is closed. Use this to hide slots.
     */
    public void onTabClose() { }

    /**
     * Called when the menu is closed. Use this to clear the items of any slots connected to temporary inventories.
     */
    public void onMenuClose() { }

    /**
     * Called when the menu is loaded to add any tab-specific slots.
     */
    public void addStorageMenuSlots(Function<Slot,Slot> addSlot) { }

    public boolean quickMoveStack(ItemStack stack) { return false; }

    public abstract void receiveMessage(LazyPacketData message);

    /**
     * Called when this tab is opened if the open packet contained additional data<br>
     * Is run before {@link #onTabOpen()}
     */
    public void OpenMessage(LazyPacketData message) { this.receiveMessage(message); }

}
