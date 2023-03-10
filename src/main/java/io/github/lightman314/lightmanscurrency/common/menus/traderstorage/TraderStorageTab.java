package io.github.lightman314.lightmanscurrency.common.menus.traderstorage;

import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class TraderStorageTab {

	public static final int TAB_TRADE_BASIC = 0;
	public static final int TAB_TRADE_STORAGE = 1;
	public static final int TAB_TRADE_ADVANCED = 2;
	
	public final TraderStorageMenu menu;
	
	protected TraderStorageTab(TraderStorageMenu menu) { this.menu = menu; }
	
	@OnlyIn(Dist.CLIENT)
	public abstract TraderStorageClientTab<?> createClientTab(TraderStorageScreen screen);
	
	/**
	 * Whether the player has permission to access this tab.
	 */
	public abstract boolean canOpen(PlayerEntity player);
	
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
	public abstract void receiveMessage(CompoundNBT message);
	
}
