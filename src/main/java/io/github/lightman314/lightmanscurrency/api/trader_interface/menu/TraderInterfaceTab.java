package io.github.lightman314.lightmanscurrency.api.trader_interface.menu;

import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public abstract class TraderInterfaceTab implements LazyPacketData.IBuilderProvider {

	public static final int TAB_INFO = 0;
	public static final int TAB_STORAGE = 1;
	public static final int TAB_TRADER_SELECT = 2;
	public static final int TAB_TRADE_SELECT = 3;
	public static final int TAB_STATS = 12;
	public static final int TAB_OWNERSHIP = 100;
	
	public final TraderInterfaceMenu menu;
	
	protected TraderInterfaceTab(TraderInterfaceMenu menu) { this.menu = menu; }

	@Nonnull
	@Override
	public final LazyPacketData.Builder builder() { return this.menu.builder(); }
	@Nonnull
	public final RegistryAccess registryAccess() { return this.menu.registryAccess(); }

	@OnlyIn(Dist.CLIENT)
	public abstract TraderInterfaceClientTab<?> createClientTab(TraderInterfaceScreen screen);
	
	/**
	 * Whether the player has permission to access this tab.
	 */
	public abstract boolean canOpen(Player player);
	
	/**
	 * Called when the tab is opened. Use this to unhide slots.
	 */
	public abstract void onTabOpen();
	
	/**
	 * Called when the tab is closed. Use this to hide slots.
	 */
	public abstract void onTabClose();
	
	public void onMenuClose() { }
	
	/**
	 * Called when the menu is loaded to add any tab-specific slots.
	 */
	public abstract void addStorageMenuSlots(Function<Slot,Slot> addSlot);
	
	public boolean quickMoveStack(ItemStack stack) { return false; }
	

	@Deprecated(since = "2.2.1.4")
	public void receiveMessage(CompoundTag ignored) {}

	public abstract void handleMessage(@Nonnull LazyPacketData message);

	
}
