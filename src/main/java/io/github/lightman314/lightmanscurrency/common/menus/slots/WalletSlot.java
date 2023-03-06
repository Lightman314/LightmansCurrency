package io.github.lightman314.lightmanscurrency.common.menus.slots;

import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class WalletSlot extends Slot {
	
	public static final ResourceLocation EMPTY_WALLET_SLOT = new ResourceLocation(LightmansCurrency.MODID, "item/empty_wallet_slot");
	public static final Pair<ResourceLocation,ResourceLocation> BACKGROUND = Pair.of(ModMenus.BLOCK_ATLAS, EMPTY_WALLET_SLOT);
	
	private final List<IWalletSlotListener> listeners = Lists.newArrayList();
	
	IInventory blacklistInventory;
	int blacklistIndex;
	
	public WalletSlot(IInventory inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	public WalletSlot addListener(IWalletSlotListener listener)
	{
		if(!listeners.contains(listener))
			listeners.add(listener);
		return this;
	}
	
	@Override
	public boolean mayPlace(@Nonnull ItemStack stack) {
		if(this.blacklistIndex >= 0 && this.blacklistInventory != null)
		{
			if(stack == this.getBlacklistedItem())
				return false;
		}
        return isValidWallet(stack);
	}
	
	public static boolean isValidWallet(ItemStack stack) {
		return stack.getItem() instanceof WalletItem && !stack.isEmpty();
	}
	
	@Override
	public Pair<ResourceLocation,ResourceLocation> getNoItemIcon() { return BACKGROUND; }
	
	public void setChanged() {
		super.setChanged();
		this.listeners.forEach(IWalletSlotListener::onWalletSlotChanged);
	}
	
	public void setBlacklist(IInventory blacklistInventory, int blacklistIndex)
	{
		this.blacklistInventory = blacklistInventory;
		this.blacklistIndex = blacklistIndex;
	}
	
	public ItemStack getBlacklistedItem()
	{
		return this.blacklistInventory.getItem(this.blacklistIndex);
	}
	
	public interface IWalletSlotListener {
		public void onWalletSlotChanged();
	}

}
