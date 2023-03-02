package io.github.lightman314.lightmanscurrency.common.menus.traderinterface;

import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class TraderInterfaceTab {

	public static final int TAB_INFO = 0;
	public static final int TAB_STORAGE = 1;
	public static final int TAB_TRADER_SELECT = 2;
	public static final int TAB_TRADE_SELECT = 3;
	public static final int TAB_OWNERSHIP = 100;
	
	public final TraderInterfaceMenu menu;
	
	protected TraderInterfaceTab(TraderInterfaceMenu menu) { this.menu = menu; }
	
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
	
	/**
	 * Sends a message to the server to notify them about an interaction made client-side.
	 */
	public abstract void receiveMessage(CompoundTag message);
	
}
