package io.github.lightman314.lightmanscurrency.common.capability;

import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IWalletHandler{

	/**
	 * The inventory containing the equipped wallet. Used for slot access.
	 */
	Container getInventory();
	
	/**
	 * The currently equipped wallet on the player.
	 */
	ItemStack getWallet();
	
	/**
	 * Sets the currently equipped wallet on the player.
	 */
	void setWallet(ItemStack walletStack);
	
	/**
	 * Whether the wallet should be rendered
	 */
	boolean visible();
	
	/**
	 * 
	 */
	void setVisible(boolean visible);
	
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
	
	/**
	 * Save the nbt data to file
	 */
	public Tag writeTag();
	
	/**
	 * Load the nbt data from file
	 */
	public void readTag(Tag tag);
	
}
