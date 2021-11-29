package io.github.lightman314.lightmanscurrency.common.capability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public interface IWalletHandler{

	/**
	 * The inventory containing the equipped wallet. Used for slot access.
	 */
	IInventory getInventory();
	
	/**
	 * The currently equipped wallet on the player.
	 */
	ItemStack getWallet();
	
	/**
	 * Sets the currently equipped wallet on the player.
	 */
	void setWallet(ItemStack walletStack);
	
	/**
	 * Gets the entity this wallet handler is attached to.
	 */
	LivingEntity getEntity();
	
	/**
	 * Returns true if the wallet has been changed, and needs to send an update packet
	 */
	boolean isDirty();
	
	/**
	 * Removes the dirty flag, called when an update packet is sent.
	 */
	void clean();
	
}
