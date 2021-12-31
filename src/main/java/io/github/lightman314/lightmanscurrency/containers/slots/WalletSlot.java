package io.github.lightman314.lightmanscurrency.containers.slots;

import io.github.lightman314.lightmanscurrency.items.WalletItem;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class WalletSlot extends Slot{
	
	public static final ResourceLocation EMPTY_WALLET_SLOT = new ResourceLocation(LightmansCurrency.MODID, "items/empty_wallet_slot");
	
	private final List<IWalletSlotListener> listeners = Lists.newArrayList();
	IInventory blacklistInventory;
	int blacklistIndex;
	
	public WalletSlot(IInventory inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		if(this.blacklistIndex >= 0 && this.blacklistInventory != null)
		{
			if(stack == this.getBlacklistedItem())
				return false;
		}
        return isValidWallet(stack);
	}
	
	public WalletSlot addListener(IWalletSlotListener listener)
	{
		if(!listeners.contains(listener))
			listeners.add(listener);
		return this;
	}
	
	public static boolean isValidWallet(ItemStack stack) {
		return stack.getItem() instanceof WalletItem;
	}
	
	@Override
	public Pair<ResourceLocation,ResourceLocation> getBackground() {
		return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, EMPTY_WALLET_SLOT);
	}
	
	@Override
	public void onSlotChanged() {
		super.onSlotChanged();
		this.listeners.forEach(listener -> listener.onWalletSlotChanged());
	}
	
	public interface IWalletSlotListener{
		public void onWalletSlotChanged();
	}
	
	ItemStack getBlacklistedItem()
	{
		return this.blacklistInventory.getStackInSlot(this.blacklistIndex);
	}
	
	public void setBlacklist(IInventory blacklistInventory, int blacklistIndex)
	{
		this.blacklistInventory = blacklistInventory;
		this.blacklistIndex = blacklistIndex;
	}

}
