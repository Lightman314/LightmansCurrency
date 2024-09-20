package io.github.lightman314.lightmanscurrency.common.capability.wallet;

import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.misc.IEasyTickable;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IWalletHandler extends IMoneyHandler, IClientTracker, IEasyTickable {

	/**
	 * The currently equipped wallet on the player.
	 */
	ItemStack getWallet();
	
	/**
	 * Sets the currently equipped wallet on the player.
	 */
	void setWallet(ItemStack walletStack);
	
	/**
	 * Forcibly sets the internal saved wallet to the given item.
	 * Used for server -> client synchronization, should not be used for general purposes.
	 */
	void syncWallet(ItemStack walletStack);
	
	/**
	 * Whether the wallet should be rendered
	 */
	boolean visible();
	
	/**
	 * Sets whether the wallet should be rendered
	 */
	void setVisible(boolean visible);
	
	/**
	 * Gets the entity this wallet handler is attached to.
	 */
	LivingEntity entity();
	
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
	CompoundTag save();

	/**
	 * Load the nbt data from file
	 */
	void load(CompoundTag tag);
	
}
