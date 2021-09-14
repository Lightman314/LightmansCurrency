package io.github.lightman314.lightmanscurrency.extendedinventory;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public interface IWalletInventory {
	
	/**
	 * Gives the background wallet array that is used server-side to check for changes with the existing inventory so updates can be sent to the relevant clients.
	 */
	public NonNullList<ItemStack> getWalletArray();
	/**
	 * Gives the wallet inventory array.
	 */
	public NonNullList<ItemStack> getWalletItems();
	/**
	 * Copies the given wallet inventory contents into this wallet inventory.
	 */
	public void copyWallet(IWalletInventory inventory);
	
}
