package io.github.lightman314.lightmanscurrency.common.menus.tabbed;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.function.Function;

public abstract class EasyMenuTab<M extends IEasyTabbedMenu<T>,T extends EasyMenuTab<M,T>> implements LazyPacketData.IBuilderProvider, IClientTracker {

    public final M menu;

    @Override
    public final boolean isClient() { return this.menu.isClient(); }

    public EasyMenuTab(@Nonnull M menu) { this.menu = menu; }

    @Nonnull
    @Override
    public final LazyPacketData.Builder builder() { return this.menu.builder(); }

    @Nonnull
    public abstract Object createClientTab(@Nonnull Object screen);

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
     * Called when the menu is closed. Use this to clear the contents of any slots connected to temporary inventories.
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
    public void OpenMessage(@Nonnull LazyPacketData message) { this.receiveMessage(message); }

}