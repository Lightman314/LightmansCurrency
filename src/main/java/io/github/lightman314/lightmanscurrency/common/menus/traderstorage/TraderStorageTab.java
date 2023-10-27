package io.github.lightman314.lightmanscurrency.common.menus.traderstorage;

import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class TraderStorageTab {

	//0-9 "Basic Tabs"
	public static final int TAB_TRADE_BASIC = 0;
	public static final int TAB_TRADE_STORAGE = 1;
	public static final int TAB_TRADE_ADVANCED = 2;
	public static final int TAB_TRADE_MISC = 3;

	//10-49 "Settings and Logs"
	public static final int TAB_TRADER_LOGS = 10;
	public static final int TAB_TRADER_SETTINGS = 11;

	//50 "Tax Info"
	public static final int TAB_TAX_INFO = 50;

	//100 & 101 "Trade Rules"
	public static final int TAB_RULES_TRADER = 100;
	public static final int TAB_RULES_TRADE = 101;
	
	public final TraderStorageMenu menu;
	
	protected TraderStorageTab(TraderStorageMenu menu) { this.menu = menu; }

	/**
	 * Input is of type TraderStorageScreen
	 * Output should be of type TraderStorageClientTab<?>
	 */
	public abstract Object createClientTab(Object screen);
	
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
	@Deprecated(since = "2.1.2.4")
	public void receiveMessage(CompoundTag message) {}

	public abstract void receiveMessage(LazyPacketData message);
	
}
